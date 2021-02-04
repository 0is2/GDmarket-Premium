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
        - [시나리오 검증](#시나리오-검증)
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

## CQRS

## Correlation

## Req/Res

## Gateway

## 시나리오 검증

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

* (2) 컨테이너라이징: 서비스 생성 확인
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

## Polyglot

## Self-healing
