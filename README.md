# GDmarket-Premium
GDmarket : 근대마켓 - 근거리 대여 마켓 (Premium)

![image](./img/logo-prm.jpg)

# Table of contents
- 근대마켓
   - [체크포인트](#체크포인트)
   - [서비스 시나리오](#서비스-시나리오)
        - [기능적 요구사항](#기능적-요구사항)
        - [비기능적 요구사항](#비기능적-요구사항)
   - [설계](#설계)
        - [기본 모델](#기본-모델)
        - [Premium 모델](#Premium-모델)
   - [구현](#구현)
        - [Saga](#Saga)
        - [CQRS](#CQRS)
        - [Correlation](#Correlation)
        - [Req/Res](#Req/Res)
        - [Gateway](#Gateway)
        - [구현 검증](#구현-검증)
   - [운영](#운영)
      - [Deploy](#Deploy)
      - [CirCuit Breaker](#CirCuit-Breaker)
      - [Autoscale](#Autoscale)
      - [무정지 재배포](#무정지-재배포)
      - [Config Map](#Config-Map)
      - [Polyglot](#Polyglot)
      - [Self-healing](#Self-healing)


# 체크포인트

1. Saga
1. CQRS
1. Correlation
1. Req/Res
1. Gateway
1. Deploy / Pipeline
1. Circuit Breaker
1. Autoscale (HPA)
1. Zero-downtime deploy (Readiness Probe)
1. Config Map/ Persistence Volume
1. Polyglot
1. Self-healing (Liveness Probe)

# 서비스 시나리오

## 기능적 요구사항
1. 물건관리자는 물건을 등록할 수 있다
2. 물건관리자는 물건을 삭제할 수 있다.
3. 대여자는 물건을 선택하여 예약한다.
4. 대여자는 예약을 취소할 수 있다.   
5. 예약이 완료되면 해당 물건은 대여불가 상태로 변경된다.
6. 대여자가 결제한다.
7. 대여자는 결제를 취소할 수 있다.
8. 물건관리자는 물건을 대여해준다.
9. 대여자가 대여요청을 취소할 수 있다.
10. 물건이 반납되면 물건은 대여가능 상태로 변경된다.
11. 물건관리자는 물건 통합상태를 중간중간 조회할 수 있다.
12. (Premium) 물건이 등록되면 등록 알람이 발생한다. 
13. (Premium) 물건이 삭제되면 삭제 알람이 발생한다. 


## 비기능적 요구사항
1. 트랜잭션
    1. 결제승인이 되지 안은 건은 결제요청이 완료되지 않아야한다. Sync 호출
    2. 등록 알람이 발생되지 않은 건은 물건등록이 완료되지 않아야 한다. Sync 호출 (Premium) 
2. 장애격리
    1. 물건관리시스템이 수행되지 않더라도 대여 요청은 365일 24시간 받을 수 있어야 한다. > Async
    2. 결제시스템이 과중되면 결제요청을 잠시동안 받지 않고 결제를 잠시 후에 하도록 유도한다. > Circuit breaker
    3. (Premium) 알람시스템이 수행되지 않더라도 물건 삭제 요청은 365일 24시간 받을 수 있다 > Async
    4. (Premium) 알람시스템이 과중되면 물건등록을 잠시동안 받지 않고 알람을 잠시 후에 하도록 유도한다. > Circuit breaker
3. 성능
    1. 물건관리자가 등록한 물건의 통합상태를 별도로 확인할 수 있어야 한다. > CQRS


# 설계

## 기본 모델
* item, reservation, payment 서비스 구현
![model](https://user-images.githubusercontent.com/26623768/106826542-75c1e380-66ca-11eb-9ab5-12cabcdac6fa.png)

## Premium 모델
* alarm 서비스가 추가됨
![model-premium](https://user-images.githubusercontent.com/26623768/106826521-6b9fe500-66ca-11eb-9fd2-657d3f9ac2c0.png)


# 구현

## Saga
* Pub/Sub을 구현한다.
* (Pub) 호출 서비스 및 Event : item / item이 삭제됨
```java
   // item > Item.java
   
    @PreRemove
    public void onPreRemove() {
        ItemDeleted itemDeleted = new ItemDeleted();
        itemDeleted.setItemNo(this.getItemNo());
        itemDeleted.setAlarmStatus("DeleteAlerted");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(itemDeleted);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }
        KafkaProcessor processor = ItemApplication.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();
        outputChannel.send(org.springframework.integration.support.MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());
        System.out.println("@@@@@@@ itemDeleted to Json @@@@@@@");
        System.out.println(itemDeleted.toJson());
    }
```
![9 itemDelete](https://user-images.githubusercontent.com/26623768/106829800-38605480-66d0-11eb-8419-8c868be631d1.PNG)

* (Sub) 피호출 서비스 및 Policy : alarm / alarm 상태 변경 (Alerted -> DeleteAlerted)
```java
   // alarm > PolicyHandler.java
   
   @StreamListener(KafkaProcessor.INPUT)
    public void wheneverItemDeleted_(@Payload ItemDeleted itemDeleted){
        if(itemDeleted.isMe()){
            System.out.println("##### listener  : " + itemDeleted.toJson());
            if("DeleteAlerted".equals(itemDeleted.getAlarmStatus())){
                Alarm alarm = (Alarm) alarmManagementRepository.findByItemNo(itemDeleted.getItemNo()).get(0);
                alarm.setAlarmStatus("DeleteAlerted");
                alarmManagementRepository.save(alarm);
            }
        }
    }
```
![10 wheneverItemDeleted_](https://user-images.githubusercontent.com/26623768/106829806-38f8eb00-66d0-11eb-9bad-9c37b3b5f618.PNG)

## CQRS
* command와 query의 역할을 분리한다. (view 구현)
* item 이 등록될 때 알람상태(AlarmStatus)를 View를 통해 확인할 수 있도록 구현
* item 코드 구현
```java
// item > Item.java

    @PostPersist
    public void onPostPersist(){
        ItemRegistered itemRegistered = new ItemRegistered();
        itemRegistered.setItemNo(this.getItemNo());
        itemRegistered.setItemName(this.getItemName());
        itemRegistered.setItemPrice(this.getItemPrice());
        itemRegistered.setItemStatus("Rentable");
        itemRegistered.setRentalStatus("NotRenting");
        itemRegistered.setAlarmStatus("Alerted");
        
        // view를 위해 Kafka Send
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(itemRegistered);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }
        KafkaProcessor processor = ItemApplication.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();
        outputChannel.send(org.springframework.integration.support.MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());

        System.out.println("@@@@@@@ ItemRegistered to Json @@@@@@@");
        System.out.println(itemRegistered.toJson());
   }
```
![11 A cqrs](https://user-images.githubusercontent.com/26623768/106830224-faaffb80-66d0-11eb-9902-c23126b28f5e.PNG)

* view 코드 구현
```java
// item > ItemInfoViewHandler.java

   @StreamListener(KafkaProcessor.INPUT)
    public void whenItemRegistered_then_CREATE_1 (@Payload ItemRegistered itemRegistered) {
        try {
            if (itemRegistered.isMe()) {
                // view 객체 생성
                ItemInfo itemInfo= new ItemInfo();
                // view 객체에 이벤트의 Value 를 set 함
                itemInfo.setItemNo(itemRegistered.getItemNo());
                itemInfo.setItemName(itemRegistered.getItemName());
                itemInfo.setItemStatus(itemRegistered.getItemStatus());
                itemInfo.setItemPrice(itemRegistered.getItemPrice());
                itemInfo.setAlarmStatus(itemRegistered.getAlarmStatus());
                // view 레파지 토리에 save
                itemInfoRepository.save(itemInfo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
```
![11 cqrs](https://user-images.githubusercontent.com/26623768/106830225-fbe12880-66d0-11eb-9824-9750188a2944.PNG)


## Correlation
* 각 마이크로 서비스는 상호 관련 키를 갖는다.
* CQRS 구현을 위해, Alarm과 ItemInfo는 상호 관련 키 'itemNo', 'alarmStatus'를 갖는다.
* Alarm.java <br>
![13 alarm](https://user-images.githubusercontent.com/26623768/106830470-8cb80400-66d1-11eb-8d7d-48e837d4e53f.PNG)
* ItemInfo.java <br>
![12 item info](https://user-images.githubusercontent.com/26623768/106830466-8b86d700-66d1-11eb-9aec-8fbc9b5204b9.PNG)

## Req/Res
* Sync 호출을 구현한다.
* (Req) 호출 서비스 구현
```java
// item.java > onPostPersist()

// alarm REQ/RES
System.out.println("@@@ Alarm @@@");
System.out.println("@@@ ItemNo : " + getItemNo());

gdmarketpremium.external.Alarm alarm = new gdmarketpremium.external.Alarm();
alarm.setAlarmStatus("Alerted");
alarm.setAlarmNo(getItemNo());
alarm.setItemNo(getItemNo());
```
![14 Item onPostPersist](https://user-images.githubusercontent.com/26623768/106830892-48793380-66d2-11eb-8234-1124429557ce.PNG)

* (Res) 피호출 서비스 구현
```java
// AlarmService.java

@FeignClient(name="alarm", url="${api.alarm.url}")
public interface AlarmService {
 @RequestMapping(method= RequestMethod.POST, path="/alarms")
    public void alert(@RequestBody Alarm alarm);
}
```
![15 alarmservice](https://user-images.githubusercontent.com/26623768/106830894-49aa6080-66d2-11eb-9dac-d44fc1cf22b3.PNG)


## Gateway
* 각 마이크로서비스는 gateway를 통해서 호출할 수 있다
* gateway 서비스 > application.yml 파일에 구현한다.
* local 세팅 <br>
![16 local](https://user-images.githubusercontent.com/26623768/106831101-a6a61680-66d2-11eb-84c5-93facf55cf6a.PNG)
* docker 세팅 <br>
![17 docker](https://user-images.githubusercontent.com/26623768/106831105-a7d74380-66d2-11eb-8259-62393451166c.PNG)


## 구현 검증

* item 등록함
```
http POST item:8080/items/ itemName=Camera itemPrice=100 itemStatus=Rentable rentalStatus=NotRenting
```
![1 아이템등록](https://user-images.githubusercontent.com/26623768/106826496-604cb980-66ca-11eb-842c-f92f4fbabc54.png)

* Sync 호출로 alarm 생성됨 (Req/Res)
```
http alarm:8080/alarms
```
![2 REQRES 생성됨](https://user-images.githubusercontent.com/26623768/106826498-617de680-66ca-11eb-81ae-9b76858c89ac.PNG)

* CQRS : alarmStatus를 확인할 수 있음
```
http item:8080/itemInfoes
```
![5 view](https://user-images.githubusercontent.com/26623768/106826512-65aa0400-66ca-11eb-9c70-bc9f41158123.PNG)

* item 삭제함
```
http DELETE item:8080/items/1
```
![3 아이템삭제](https://user-images.githubusercontent.com/26623768/106826504-6347aa00-66ca-11eb-8f10-5412072f0ba6.PNG)

* Async 호출로 alarm 상태 변경됨 (Alerted -> DeleteAlerted) (Pub/Sub) 
```
http alarm:8080/alarms
```
![4 PUBSUB 실행됨](https://user-images.githubusercontent.com/26623768/106826508-6478d700-66ca-11eb-820b-4637d2809d48.PNG)

* gateway로 reservation 서비스 GET 호출
```
http gateway:8080/reservations
```
![18 gateway](https://user-images.githubusercontent.com/26623768/106831219-dfde8680-66d2-11eb-80f1-c738485b7940.PNG)

# 운영

## Deploy
* (1-1) 컨테이너라이징 : 디플로이 생성 확인 (gateway 서비스는 api 사용)
```
kubectl create deploy gateway --image=gdpremiumacr.azurecr.io/gateway:latest
```
![6-0 kubectl create deploy](https://user-images.githubusercontent.com/26623768/106829284-2fbb4e80-66cf-11eb-8336-991b654b299c.PNG)

* (1-2) 컨테이너라이징 : 디플로이 생성 확인 (그 외 서비스는 yml 사용)
```
kubectl apply -f kubernetes/deployment.yml
```
![6 kubectl apply](https://user-images.githubusercontent.com/26623768/106829283-2f22b800-66cf-11eb-978e-9b580d02c761.PNG)

* (2) 컨테이너라이징: 서비스 생성 확인 (gateway 포함모든 서비스 동일)
```
kubectl expose deploy gateway --type="ClusterIP" --port=8080
```
![7 kubectl expose deploy](https://user-images.githubusercontent.com/26623768/106829285-3053e500-66cf-11eb-8509-18d592667dce.PNG)

* 모든 서비스가 잘 Running 됨을 확인
```
kubectl get all
```
![8 kube get all](https://user-images.githubusercontent.com/26623768/106829383-61ccb080-66cf-11eb-9663-a03fc48f952c.PNG)


## CirCuit Breaker

## Autoscale

## 무정지 재배포

## Config Map
* alarm 서비스를 REQ로 호출하는 item 서비스에 config map을 구현한다.
* item > application.yml : local  <br>
![19 item app local](https://user-images.githubusercontent.com/26623768/106831460-4d8ab280-66d3-11eb-9aba-d70a73086b12.PNG)
* item > application.yml : docker <br>
![20 item app docker](https://user-images.githubusercontent.com/26623768/106831461-4e234900-66d3-11eb-9b32-763d20f97a14.PNG)
* item > deployment.yml : env 세팅 <br>
![21 item deployment env](https://user-images.githubusercontent.com/26623768/106831464-4ebbdf80-66d3-11eb-8ce0-d114aea29128.PNG)
* item > external > AlarmService.java : env 사용 <br>
![22 item external alarmservice](https://user-images.githubusercontent.com/26623768/106831466-4ebbdf80-66d3-11eb-9f42-152441265794.PNG)
* config map 생성 및 확인
```
# 생성
kubectl create configmap newurl --from-literal=url=http://alarm:8080

# 확인
kubectl get configmap newurl -o yaml
```
![23 configmap 확인](https://user-images.githubusercontent.com/26623768/106831798-e4f00580-66d3-11eb-93ba-9bca68dcb29f.PNG)

## Polyglot
* 다형성을 만족하도록 구현한다.
* item, reservation 서비스는 H2 DB로 구현하고, 그와 달리 payment, alarm 서비스의 경우 Hsql DB로 구현한다.
* item, reservation 서비스의 pom.xml 설정 <br>
![1 p1](https://user-images.githubusercontent.com/26623768/106831962-2da7be80-66d4-11eb-9cbe-06a1cea6eb97.png)

* payment, alarm 서비스의 pom.xml 설정 <br>
![1 p2](https://user-images.githubusercontent.com/26623768/106831964-2ed8eb80-66d4-11eb-9d65-ddb6fb7eaabd.png)

## Self-healing
