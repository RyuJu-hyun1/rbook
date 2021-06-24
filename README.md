![image](https://user-images.githubusercontent.com/84724396/121802716-cb119100-cc78-11eb-8d9d-5cd9ed9102bf.png)


# 서비스 시나리오

### 기능적 요구사항

1. 책 대여
   - 1)사용자가 책 대여를 신청한다.
   - 2)책 재고를 체크하고 재고를 1개 감소한다.
   - 3)청구서가 등록된다.
2. 책 반납
   - 1)사용자가 책을 반납한다.
   - 2)책 재고가 1개 증가한다.
   - 3)결재가 완료된다.
3. 사용자는 책 대여와 과금 이력을 조회한다.

### 비기능적 요구사항

1. 트랜잭션
   - 1)책 재고가 1개 이상일때만 대여할 수 있어야 한다. -> Sync 호출
   - 2)책이 대여되면 재고가 1개 감소하고 반납되면 재고가 1개 증가한다. -> SAGA, 보상 트랜젝션

2. 장애격리
   - 1)과금 관리 서비스가 수행되지 않더라도 365일 24시간 책을 대여할 수 있어야 한다. -> Async(event-driven) , Eventual consistency
   - 2)과금 관리 서비스가 수행되지 않더라도 365일 24시간 책을 반납할 수 있어야 한다. -> Async(event-driven) , Eventual consistency
   - 3)책 관리 시스템이 과중되면 대여를 잠시 동안 받지 않고 대여를 잠시 후에 하도록 유도한다. -> Circuit breaker

3. 성능
   - 1)사용자가 대여와 청구 이력을 조회 할 수 있도록 성능을 고려하여 별도의 view로 구성한다.> CQRS


# 체크포인트

1. Saga
2. CQRS
3. Correlation
4. Req/Resp
5. Gateway
6. Deploy/ Pipeline
7. Circuit Breaker
8. Autoscale (HPA)
9. Zero-downtime deploy (Readiness Probe)
10. Config Map/ Persistence Volume
11. Polyglot
12. Self-healing (Liveness Probe)


# 분석/설계

### 1. Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/Yb3C3fFMlXWxp7KbYaVj0AG6Wd23/mine/fab3139d46ea13e0ed0108eb96c3f63c


### 2. 이벤트 도출
![image](https://user-images.githubusercontent.com/84724396/121798830-90e9c480-cc63-11eb-8cf6-f365ac151763.png)


### 3. 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/84724396/121798856-a4952b00-cc63-11eb-9f26-a1c626d7c8b2.png)

    잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
       - 책 선택됨, 대여 버튼 클릭됨, 결제 버튼 클릭됨 :  UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외
	

### 4. Policy, Command, Actor 부착
![image](https://user-images.githubusercontent.com/84724396/121799480-5f72f800-cc67-11eb-9832-045c9fc51997.png)


### 5. Aggregate로 묶기
![image](https://user-images.githubusercontent.com/84724396/121803071-b46c3980-cc7a-11eb-9739-894a3fc8500a.png)

    - 대여, 책, 과금이력 Aggregate을 생성하고 그와 연결된 command 와 event, Polocy를 트랜잭션이 유지되어야 하는 단위로 묶어줌


### 6. Bounded Context로 묶기
![image](https://user-images.githubusercontent.com/84724396/121803081-c352ec00-cc7a-11eb-8ff0-39bf2989d00f.png)

    도메인 서열 분리 
       - Core Domain : 대여관리, 책관리 - 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준을 99.999% 목표, 배포주기는 app 의 경우 1주일 1회 미만
       - Supporting Domain : 과금관리, MyPage(view) - 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.


### 7. Policy의 이동 (Choreography)
![image](https://user-images.githubusercontent.com/84724396/121803098-d4036200-cc7a-11eb-9d94-17e03a772c9a.png)


### 8. Context Mapping (점선은 Pub/Sub, 실선은 Req/Resp)
![image](https://user-images.githubusercontent.com/84724396/121803111-e41b4180-cc7a-11eb-8d27-40b449011de6.png)


### 9. 완성된 모형
![image](https://user-images.githubusercontent.com/84724396/121802510-a36df900-cc77-11eb-9546-089f03e26f10.png)


### 10. 기능적 요구사항 검증
![image](https://user-images.githubusercontent.com/84724396/121803130-02813d00-cc7b-11eb-9250-b5f122130faf.png)


1. 책 대여
   - 1)사용자가 책 대여를 신청한다. (OK)
   - 2)책 재고를 체크하고 재고를 1개 감소한다. (OK)
    3)청구서가 등록된다. (OK)
2. 책 반납
   - 1)사용자가 책을 반납한다. (OK)
   - 2)책 재고가 1개 증가한다. (OK)
   - 3)결재가 완료된다. (OK)
3. 사용자는 책 대여와 과금 이력을 조회한다. (OK)


### 11. 비기능 요구사항 검증
![image](https://user-images.githubusercontent.com/84724396/121802590-1aa38d00-cc78-11eb-801c-de8aef7ab8d2.png)


    - 1)책 재고가 1개 이상일때만 대여할 수 있어야 한다. -> Req/Res
    - 2)책이 대여되면 재고가 1개 감소하고 반납되면 재고가 1개 증가한다. -> SAGA, 보상 트랜젝션
    - 3)과금 관리 서비스가 수행되지 않더라도 365일 24시간 책을 대여할 수 있어야 한다. -> Pub/sub
    - 4)과금 관리 서비스가 수행되지 않더라도 365일 24시간 책을 반납할 수 있어야 한다. -> Pub/sub
    - 5)책 관리 시스템이 과중되면 대여를 잠시 동안 받지 않고 대여를 잠시 후에 하도록 유도한다. -> Circuit breaker
    - 6)사용자가 대여와 청구 이력을 조회 할 수 있도록 성능을 고려하여 별도의 view로 구성한다.> CQRS


### 12. Hexagonal Architecture Diagram 도출 (Polyglot)
![image](https://user-images.githubusercontent.com/84724396/121799592-fb046880-cc67-11eb-9314-08926db80466.png)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리
    - rent의 경우 Polyglot 검증을 위해 Hsql로 셜계


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 구현한 각 서비스의 실행방법은 아래와 같다. (각자의 포트넘버는 8081 ~ 8084, 8088 이다)

```
cd rent
mvn spring-boot:run

cd book
mvn spring-boot:run 

cd billing
mvn spring-boot:run  

cd mypage
mvn spring-boot:run  

cd gateway
mvn spring-boot:run 
```

### 1. DDD 의 적용

각 서비스 내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다 (예: book)

![image](https://user-images.githubusercontent.com/84724396/122665595-154bc280-d1e3-11eb-8470-c4c534ef169d.png)

Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

![image](https://user-images.githubusercontent.com/84724396/123199440-d753e480-d4e9-11eb-9833-1f237af2c0eb.png)


### 2. 폴리글랏 퍼시스턴스

H2 DB인 book, billing과 달리 rent는 Hsql으로 구현하여 MSA간 서로 다른 종류의 DB간에도 문제 없이 동작하여 다형성을 만족하는지 확인하였다. 

rent의 pom.xml 설정

![image](https://user-images.githubusercontent.com/84724396/122640079-63f05280-d138-11eb-8f9f-447729df12c5.png)

book, billing의 pom.xml 설정

![image](https://user-images.githubusercontent.com/84724396/122640113-9bf79580-d138-11eb-952c-b7bd8207f966.png)


### 3. Gateway 적용

API Gateway를 통하여 마이크로 서비스들의 진입점을 통일할 수 있다. 다음과 같이 Gateway를 적용하였다.
```
gateway > applitcation.yml 설정
```

![image](https://user-images.githubusercontent.com/84724396/122665922-36151780-d1e5-11eb-9779-e9d0870a6f95.png)
![image](https://user-images.githubusercontent.com/84724396/122665929-40cfac80-d1e5-11eb-83d9-bbe402f73f57.png)


### 4. 시나리오 검증 (Gateway 사용, Correlation)

Correlation
```
rent, book, billing, myPage 서비스는 Correlation-key 로 rentid, bookid, billid 값을 전달받아 
서비스간 연관된 처리를 정확하게 구현하고 있습니다.
```

4.1 책 등록 (bookid=1, stock: 5개)
```
http POST http://localhost:8088/books bookid=1 stock=5
http POST http://localhost:8088/books bookid=2 stock=1
```
![image](https://user-images.githubusercontent.com/84724396/123202556-bf7f5f00-d4ef-11eb-955e-746459ddb49b.png)

4.2 책 대여 (userid=100, bookid=1) -> rentid=2 생성됨
```
http POST localhost:8088/rents userid=100 bookid=1
```
![image](https://user-images.githubusercontent.com/84724396/123205900-a5487f80-d4f5-11eb-8999-79b4a5234c46.png)

4.3 책 재고(stock)가 1개 감소한다. (bookid=1, stock: 4개)
```
http GET http://localhost:8088/books/1 
```
![image](https://user-images.githubusercontent.com/84724396/123202892-7085f980-d4f0-11eb-9720-1c37b4faf0f4.png)

4.4 rentid=2 에 대한 청구서가 등록된다. -> billingid=2 생성됨
```
http GET http://localhost:8088/billings
```

![image](https://user-images.githubusercontent.com/84724396/123203000-9d3a1100-d4f0-11eb-828d-ee9e2aa69e45.png)

4.5 책 반납 (rentid=2)
```
http PATCH localhost:8088/rents/2 status="반납"
```

![image](https://user-images.githubusercontent.com/84724396/123203176-e5593380-d4f0-11eb-9e43-e7327fc3a36f.png)

4.6 반납한 책 (bookid=1) 재고(stock)가 1개 증가한다. (5개)
```
http GET http://localhost:8088/books/1
```

![image](https://user-images.githubusercontent.com/84724396/123203339-28b3a200-d4f1-11eb-9bca-10097e69b9d4.png)

4.7 rentid=2 에 대한 청구서(billingid=2)가 결재완료 된다. 
```
http GET http://localhost:8088/billings/2
```

![image](https://user-images.githubusercontent.com/84724396/123203430-4f71d880-d4f1-11eb-80d4-d163915b8a4c.png)


### 4. 동기식 호출 과 Fallback 처리

비기능적 요구사항 중 하나인 '책 재고가 1개 이상일때만 대여할 수 있어야 한다.' 를 충족하기 위해
대여(rent) -> 책(book) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 책 재고 확인 서비스를 호출하기 위하여 FeignClient를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

![image](https://user-images.githubusercontent.com/84724396/122668480-2dc3d900-d1f3-11eb-9f30-0b0dfaa44083.png)

- 책 대여 요청을 받으면 책 재고 확인을 요청하도록 처리

(rent) Rent.java (Entity)

![image](https://user-images.githubusercontent.com/84724396/122668694-3ff24700-d1f4-11eb-9130-fad3cf066dc1.png)

- [검증1] 동기식 호출이 적용되서 Book 시스템이 장애가 나면 대여를 하지 못 한다는 것을 확인

```
#book 서비스를 잠시 내려놓음 (ctrl+c)

#대여하기(rent)
http POST localhost:8081/rents userid=200 bookid=2   #Fail
```
-------이미지 교체
![image](https://user-images.githubusercontent.com/73699193/98072284-04934a00-1ea9-11eb-9fad-40d3996e109f.png)

```
#book 서비스 재기동
cd book
mvn spring-boot:run

#대여하기(rent)
http POST localhost:8081/rents userid=200 bookid=2   #Success
```
-------이미지 교체
![image](https://user-images.githubusercontent.com/73699193/98074359-9f8e2300-1ead-11eb-8854-0449a65ff55c.png)


- [검증2] 책 재고가 0이면 대여를 하지 못 한다는 비기능 요구사항 확인

```
#책 재고 확인
http GET localhost:8082/books/2   #bookid=2 의 재고 0

#대여하기(rent)
http POST localhost:8081/rents userid=200 bookid=2   #Fail
```
-------이미지 

### 5. 비동기식 호출 / 시간적 디커플링 / 장애격리 

대여(rent)가 완료된 후에 청구(billing)으로 이를 알려주는 행위와 반납(return)이 완료된 후에 청구(billing)으로 이를 알려주는 행위는 비 동기식으로 처리해서 대여와 반납이 블로킹 되지 않아도 처리 한다.
 
- 대여가 완료되었다(returned)는 도메인 이벤트를 카프카로 송출한다(Publish)

![image](https://user-images.githubusercontent.com/84724396/122671181-68803e00-d200-11eb-9503-11f20445a3f0.png)

- 청구(billing)에서는 대여 완료(rented) 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다.
- 대여 완료된 (rented) 정보를 billing의 Repository에 저장한다.

![image](https://user-images.githubusercontent.com/84724396/122673339-e5181a00-d20a-11eb-9123-4f2dc14727d8.png)

청구(billing)시스템은 대여(rent)/책(book)와 완전히 분리되어있으며(sync transaction 없음), 이벤트 수신에 따라 처리되기 때문에, 청구(billing)이 유지보수로 인해 잠시 내려간 상태라도 대여를 하는데 문제가 없다.(시간적 디커플링, 장애 격리)

```
#청구(billing) 서비스를 잠시 내려놓음 (ctrl+c)

#대여(rent)
http POST localhost:8081/rents userid=300 bookid=1

#대여 상태 확인 (myPage)
http GET localhost:8084/myPages    # billid, fee, billstatus 값이 없음
```
-------- 이미지 교체

![image](https://user-images.githubusercontent.com/73699193/98078301-2b577d80-1eb5-11eb-9d89-7c03a3fa27dd.png)
```
#청구(billing) 서비스 기동
cd billing
mvn spring-boot:run

#대여 상태 확인 (myPage)
http GET localhost:8084/myPages    # billid, fee, billstatus 값이 update 됨
```

-------- 이미지 교체

![image](https://user-images.githubusercontent.com/73699193/98078837-2cd57580-1eb6-11eb-8850-a8c621410d61.png)

### 6. CQRS 

mypage에서 rent와 billing 정보를 조회한다.
```
- rent 정보 : userid=100, bookid=1, status=반납
- billing 정보 : billind=2, fee=1000, status=Paid
```

![image](https://user-images.githubusercontent.com/84724396/123204314-cc518200-d4f2-11eb-9723-a6e83d3e4845.png)


# 운영

## 1. Deploy / Pipeline

### 1.1 namespace 생성
```
kubectl create ns rbook
```
--------이미지 교체

![image](https://user-images.githubusercontent.com/73699193/97960790-6d20ef00-1df5-11eb-998d-d5591975b5d4.png)

### 1.2 git에서 소스 가져오기
```
git clone https://github.com/rbook/app.git
```

--------이미지 교체

![image](https://user-images.githubusercontent.com/73699193/98089346-eb4cc680-1ec5-11eb-9c23-f6987dee9308.png)


### 1.3 Build 하기 (예: rent)
```
cd /home/project/rbook/rent
mvn clean
mvn compile
mvn package
```

--------이미지 교체

![image](https://user-images.githubusercontent.com/73699193/98089442-19320b00-1ec6-11eb-88b5-544cd123d62a.png)


### 1.4 Docker Image Push/deploy/서비스 생성 (예: rent)-- 명령어 수정
```
cd /home/project/rbook/rent
az acr build --registry skcc1team --image skcc1team.azurecr.io/bike:latest . 
kubectl create deploy rent --image=skcc1team.azurecr.io/bike:latest -n rbook  
kubectl expose deploy rent --type=ClusterIP --port=8080 -n rbook
```

--------이미지 교체

![image](https://user-images.githubusercontent.com/73699193/98089685-6dd58600-1ec6-11eb-8fb9-80705c854c7b.png)

### 1.5 yml파일 이용한 deploy
```
cd /home/project/rbook/rent
kubectl apply -f ./kubernetes/deployment.yml -n rbook
```

- deployment.yml 파일
```
namespace, image 설정
env 설정 (config Map) 
readiness 설정 (무정지 배포)
liveness 설정 (self-healing)
resource 설정 (autoscaling)
```

--------이미지 추가 (deployment.yml)

### 1.6 컨테이너라이징: Deploy 생성, 서비스 생성 확인

```
kubectl get all -n rbook
```

--------이미지 교체

![image](https://user-images.githubusercontent.com/73699193/98090560-83977b00-1ec7-11eb-9770-9cfe1021f0b4.png)


- book, billing, mypage, gateway에도 동일한 작업 반복


### 2. 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 단말앱(app)-->결제(pay) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml
feign:
  hystrix:
    enabled: true
    
hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```
![image](https://user-images.githubusercontent.com/73699193/98093705-a166df00-1ecb-11eb-83b5-f42e554f7ffd.png)

* siege 툴 사용법:
```
 siege가 생성되어 있지 않으면:
 kubectl run siege --image=apexacme/siege-nginx -n phone82
 siege 들어가기:
 kubectl exec -it pod/siege-5c7c46b788-4rn4r -c siege -n phone82 -- /bin/bash
 siege 종료:
 Ctrl + C -> exit
```
* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
siege -c100 -t60S -r10 -v --content-type "application/json" 'http://app:8080/orders POST {"item": "abc123", "qty":3}'
```
- 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 pay에서 처리되면서 다시 order를 받기 시작 

![image](https://user-images.githubusercontent.com/73699193/98098702-07eefb80-1ed2-11eb-94bf-316df4bf682b.png)

- report

![image](https://user-images.githubusercontent.com/73699193/98099047-6e741980-1ed2-11eb-9c55-6fe603e52f8b.png)

- CB 잘 적용됨을 확인


### 오토스케일 아웃

- 대리점 시스템에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:

```
# autocale out 설정
store > deployment.yml 설정
```
![image](https://user-images.githubusercontent.com/73699193/98187434-44fbd200-1f54-11eb-9859-daf26f812788.png)

```
kubectl autoscale deploy store --min=1 --max=10 --cpu-percent=15 -n phone82
```
![image](https://user-images.githubusercontent.com/73699193/98100149-ce1ef480-1ed3-11eb-908e-a75b669d611d.png)


-
- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
```
kubectl exec -it pod/siege-5c7c46b788-4rn4r -c siege -n phone82 -- /bin/bash
siege -c100 -t120S -r10 -v --content-type "application/json" 'http://store:8080/storeManages POST {"orderId":"456", "process":"Payed"}'
```
![image](https://user-images.githubusercontent.com/73699193/98102543-0d9b1000-1ed7-11eb-9cb6-91d7996fc1fd.png)

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy store -w -n phone82
```
- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다. max=10 
- 부하를 줄이니 늘어난 스케일이 점점 줄어들었다.

![image](https://user-images.githubusercontent.com/73699193/98102926-92862980-1ed7-11eb-8f19-a673d72da580.png)

- 다시 부하를 주고 확인하니 Availability가 높아진 것을 확인 할 수 있었다.

![image](https://user-images.githubusercontent.com/73699193/98103249-14765280-1ed8-11eb-8c7c-9ea1c67e03cf.png)


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscale 이나 CB 설정을 제거함


- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
kubectl apply -f kubernetes/deployment_readiness.yml
```
- readiness 옵션이 없는 경우 배포 중 서비스 요청처리 실패

![image](https://user-images.githubusercontent.com/73699193/98105334-2a394700-1edb-11eb-9633-f5c33c5dee9f.png)


- deployment.yml에 readiness 옵션을 추가 

![image](https://user-images.githubusercontent.com/73699193/98107176-75ecf000-1edd-11eb-88df-617c870b49fb.png)

- readiness적용된 deployment.yml 적용

```
kubectl apply -f kubernetes/deployment.yml
```
- 새로운 버전의 이미지로 교체
```
cd acr
az acr build --registry admin02 --image admin02.azurecr.io/store:v4 .
kubectl set image deploy store store=admin02.azurecr.io/store:v4 -n phone82
```
- 기존 버전과 새 버전의 store pod 공존 중

![image](https://user-images.githubusercontent.com/73699193/98106161-65884580-1edc-11eb-9540-17a3c9bdebf3.png)

- Availability: 100.00 % 확인

![image](https://user-images.githubusercontent.com/73699193/98106524-c152ce80-1edc-11eb-8e0f-3731ca2f709d.png)



## Config Map

- apllication.yml 설정

* default쪽

![image](https://user-images.githubusercontent.com/73699193/98108335-1c85c080-1edf-11eb-9d0f-1f69e592bb1d.png)

* docker 쪽

![image](https://user-images.githubusercontent.com/73699193/98108645-ad5c9c00-1edf-11eb-8d54-487d2262e8af.png)

- Deployment.yml 설정

![image](https://user-images.githubusercontent.com/73699193/98108902-12b08d00-1ee0-11eb-8f8a-3a3ea82a635c.png)

- config map 생성 후 조회
```
kubectl create configmap apiurl --from-literal=url=http://pay:8080 --from-literal=fluentd-server-ip=10.xxx.xxx.xxx -n phone82
```
![image](https://user-images.githubusercontent.com/73699193/98107784-5bffdd00-1ede-11eb-8da6-82dbead0d64f.png)

- 설정한 url로 주문 호출
```
http POST http://app:8080/orders item=dfdf1 qty=21
```

![image](https://user-images.githubusercontent.com/73699193/98109319-b732cf00-1ee0-11eb-9e92-ad0e26e398ec.png)

- configmap 삭제 후 app 서비스 재시작
```
kubectl delete configmap apiurl -n phone82
kubectl get pod/app-56f677d458-5gqf2 -n phone82 -o yaml | kubectl replace --force -f-
```
![image](https://user-images.githubusercontent.com/73699193/98110005-cf571e00-1ee1-11eb-973f-2f4922f8833c.png)

- configmap 삭제된 상태에서 주문 호출   
```
http POST http://app:8080/orders item=dfdf2 qty=22
```
![image](https://user-images.githubusercontent.com/73699193/98110323-42f92b00-1ee2-11eb-90f3-fe8044085e9d.png)

![image](https://user-images.githubusercontent.com/73699193/98110445-720f9c80-1ee2-11eb-851e-adcd1f2f7851.png)

![image](https://user-images.githubusercontent.com/73699193/98110782-f4985c00-1ee2-11eb-97a7-1fed3c6b042c.png)



## Self-healing (Liveness Probe)

- store 서비스 정상 확인

![image](https://user-images.githubusercontent.com/27958588/98096336-fb1cd880-1ece-11eb-9b99-3d704cd55fd2.jpg)


- deployment.yml 에 Liveness Probe 옵션 추가
```
cd ~/phone82/store/kubernetes
vi deployment.yml

(아래 설정 변경)
livenessProbe:
	tcpSocket:
	  port: 8081
	initialDelaySeconds: 5
	periodSeconds: 5
```
![image](https://user-images.githubusercontent.com/27958588/98096375-0839c780-1ecf-11eb-85fb-00e8252aa84a.jpg)

- store pod에 liveness가 적용된 부분 확인

![image](https://user-images.githubusercontent.com/27958588/98096393-0a9c2180-1ecf-11eb-8ac5-f6048160961d.jpg)

- store 서비스의 liveness가 발동되어 13번 retry 시도 한 부분 확인

![image](https://user-images.githubusercontent.com/27958588/98096461-20a9e200-1ecf-11eb-8b02-364162baa355.jpg)

