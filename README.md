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

4.1 책 등록 (bookid=1-stock:1개, bookid=2-stock:1개)
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


### 5. 동기식 호출 과 Fallback 처리

비기능적 요구사항 중 하나인 '책 재고가 1개 이상일때만 대여할 수 있어야 한다.' 를 충족하기 위해
대여(rent) -> 책(book) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

5.1.책 재고 확인 서비스를 호출하기 위하여 FeignClient를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

![image](https://user-images.githubusercontent.com/84724396/122668480-2dc3d900-d1f3-11eb-9f30-0b0dfaa44083.png)

5.2.책 대여 요청을 받으면 책 재고 확인을 요청하도록 처리

(rent) Rent.java (Entity)

![image](https://user-images.githubusercontent.com/84724396/122668694-3ff24700-d1f4-11eb-9130-fad3cf066dc1.png)

5.3.[검증1] 동기식 호출이 적용되서 Book 시스템이 장애가 나면 대여를 하지 못 한다는 것을 확인

```
1) book 서비스를 잠시 내려놓음 (ctrl+c)

2) 책 대여하기(rent) -->Fail
   http POST localhost:8088/rents userid=100 bookid=1   
```

![image](https://user-images.githubusercontent.com/84724396/123209772-1428d700-d4fc-11eb-9530-dd89f8ebb37a.png)

```
3) book 서비스 재기동 -> 책 등록
   cd book
   mvn spring-boot:run
   http POST http://localhost:8088/books bookid=1 stock=5
   http GET http://localhost:8088/books/1 

4) 책 대여하기(rent) -->성공
   http POST localhost:8088/rents userid=100 bookid=1   
```
![image](https://user-images.githubusercontent.com/84724396/123210339-d24c6080-d4fc-11eb-8fc5-fd611b9026e0.png)

![image](https://user-images.githubusercontent.com/84724396/123210386-e1331300-d4fc-11eb-8f4e-ad41c3bd6e79.png)


5.4.[검증2] 책 재고가 0이면 대여를 하지 못 한다는 비기능 요구사항 확인

```
1) 책 재고 확인 (bookid=2 stock=0)
   http GET http://localhost:8088/books/2   

2) 책 대여하기 --> Fail (책은 재고가 없어 대여가 불가합니다. 메세지 출력) 
   http POST localhost:8088/rents userid=300 bookid=2
```

![image](https://user-images.githubusercontent.com/84724396/123211530-8b5f6a80-d4fe-11eb-9afb-4a30eef9ae3e.png)


### 6. 비동기식 호출 / 시간적 디커플링 / 장애격리 

대여(rent)가 완료된 후에 billing으로 이를 알려주는 행위와 반납(return)이 완료된 후에 billing으로 이를 알려주는 행위는 비 동기식으로 처리해서 대여와 반납이 블로킹 되지 않아도 처리한다.
 
- 대여가 완료되었다(returned)는 도메인 이벤트를 카프카로 송출한다(Publish)

![image](https://user-images.githubusercontent.com/84724396/122671181-68803e00-d200-11eb-9503-11f20445a3f0.png)

- billing에서는 대여 완료(rented) 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다.
- 대여 완료된 (rented) 정보를 billing의 Repository에 저장한다.

![image](https://user-images.githubusercontent.com/84724396/122673339-e5181a00-d20a-11eb-9123-4f2dc14727d8.png)

billing 서비스는 rent, book 과 완전히 분리되어있으며(sync transaction 없음), 이벤트 수신에 따라 처리되기 때문에, 청구(billing)이 유지보수로 인해 잠시 내려간 상태라도 대여를 하는데 문제가 없다.(시간적 디커플링, 장애 격리)

```
6.1.billing 서비스를 잠시 내려놓음 (ctrl+c)

6.2.책 대여(rent) -> 성공
http POST localhost:8088/rents userid=100 bookid=1
```

![image](https://user-images.githubusercontent.com/84724396/123214227-e777be00-d501-11eb-96fa-7d66863588fb.png)

```
6.3.myPage 확인 : rent 정보는 있으나 billing 서비스가 죽어 있어 billid, fee, billstatus 값이 없음
http GET localhost:8084/myPages    
```

![image](https://user-images.githubusercontent.com/84724396/123214660-797fc680-d502-11eb-9c96-c0bfb49270e6.png)

```
6.4.billing 서비스 기동
cd billing
mvn spring-boot:run

6.5.billing 확인 --> rentid=1 에 대한 billing 데이터가 생성됨

http GET http://localhost:8088/billings
```
![image](https://user-images.githubusercontent.com/84724396/123215451-68838500-d503-11eb-8156-cf5d31f0d9b9.png)

```
6.6.myPage 확인 : billid, fee, billstatus 값이 update 됨

http GET localhost:8084/myPages
```
![image](https://user-images.githubusercontent.com/84724396/123215562-9072e880-d503-11eb-8eb7-4db6533ee567.png)

### 7. CQRS 

mypage에서 rent와 billing 정보를 조회한다.
```
7.1.책 대여 후
- rent 정보 : userid=500, bookid=1, rent status=대여
- billing 정보 : billid=3, fee=1000, billing status=Billing
```

![image](https://user-images.githubusercontent.com/84724396/123216677-c4024280-d504-11eb-8f26-a6285a4f61b1.png)

```
7.2.책 반납 후
- rent 정보 : userid=500, bookid=1, rent status=반납
- billing 정보 : billid=3, fee=1000, billing status=Paid
```

![image](https://user-images.githubusercontent.com/84724396/123216758-d8ded600-d504-11eb-8d5e-fc43c3d8e0cd.png)


# 운영

## 1. Deploy

### 1.1 namespace 생성

```
kubectl create ns rbook
```

### 1.2 git에서 소스 가져오기

```
git clone https://github.com/rbook/app.git
```

### 1.3 Build 하기 (예: rent)

```
cd /home/project/rbook/rent
mvn clean
mvn compile
mvn package
```

### 1.4 Docker Image Push/deploy/Service 생성

```
cd /home/project/rbook/rent
az acr build --registry skccrjh2 --image skccrjh2.azurecr.io/rent:v4 . 
kubectl create deploy rent --image=skccrjh2.azurecr.io/rent:v1 -n rbook  
kubectl expose deploy rent --type=ClusterIP --port=8080 -n rbook

cd /home/project/rbook/book
az acr build --registry skccrjh2 --image skccrjh2.azurecr.io/book:v1 . 
kubectl create deploy book --image=skccrjh2.azurecr.io/book:v1 -n rbook  
kubectl expose deploy book --type=ClusterIP --port=8080 -n rbook

cd /home/project/rbook/billing
az acr build --registry skccrjh2 --image skccrjh2.azurecr.io/billing:v1 . 
kubectl create deploy billing --image=skccrjh2.azurecr.io/billing:v1 -n rbook  
kubectl expose deploy billing --type=ClusterIP --port=8080 -n rbook

cd /home/project/rbook/mypage
az acr build --registry skccrjh2 --image skccrjh2.azurecr.io/mypage:v1 . 
kubectl create deploy mypage --image=skccrjh2.azurecr.io/mypage:v1 -n rbook  
kubectl expose deploy mypage --type=ClusterIP --port=8080 -n rbook

cd /home/project/rbook/gateway
az acr build --registry skccrjh2 --image skccrjh2.azurecr.io/gateway:v2 . 
kubectl create deploy gateway --image=skccrjh2.azurecr.io/gateway:v2 -n rbook  
kubectl expose deploy gateway --type=LoadBalancer --port=8080 -n rbook
```
```
kubectl get all -n rbook
```
![image](https://user-images.githubusercontent.com/84724396/123253692-b3fc5a00-d528-11eb-9066-f2e95076ef7a.png)


### 1.5 yml파일 이용한 deploy

1.5.1.deployment.yml 파일 (예: book)

![image](https://user-images.githubusercontent.com/84724396/123274061-262b6980-d53e-11eb-8279-539b01d49c4a.png)

1.5.2.Deploy/Service 생성

```
cd /home/project/rbook/rent
kubectl create -f ./kubernetes/deployment.yml -n rbook
kubectl create -f ./kubernetes/service.yaml -n rbook

cd /home/project/rbook/book
kubectl create -f ./kubernetes/deployment.yml -n rbook
kubectl create -f ./kubernetes/service.yaml -n rbook

cd /home/project/rbook/billing
kubectl create -f ./kubernetes/deployment.yml -n rbook
kubectl create -f ./kubernetes/service.yaml -n rbook

cd /home/project/rbook/mypage
kubectl create -f ./kubernetes/deployment.yml -n rbook
kubectl create -f ./kubernetes/service.yaml -n rbook
```

1.5.3.컨테이너라이징: Deploy 생성, Service 생성 확인

```
kubectl get all -n rbook
```

![image](https://user-images.githubusercontent.com/84724396/123281630-d00df480-d544-11eb-9760-a63fbebe4cb0.png)


### 2. ConfigMap

2.1.application.yml 파일 설정
- default 쪽

![image](https://user-images.githubusercontent.com/84724396/123258618-887c6e00-d52e-11eb-9ac4-7edab97c6ee3.png)

- dovker 쪽

![image](https://user-images.githubusercontent.com/84724396/123255732-24a47600-d52b-11eb-9dc6-3b70fe16879b.png)

2.2.BookService.java 파일

![image](https://user-images.githubusercontent.com/84724396/123256098-87960d00-d52b-11eb-8b5c-5fe397e8d325.png)


2.3.deployment.yaml 파일 설정

![image](https://user-images.githubusercontent.com/84724396/123269893-5f61da80-d53a-11eb-9daa-031203e4dacd.png)

2.4.configMap 생성 및 확인

![image](https://user-images.githubusercontent.com/84724396/123277167-d306e600-d540-11eb-860b-b4894afdcdf9.png)

2.5.설정한 url로 주문 호출 --> 성공

```
http POST http://20.194.57.130:8080/rents userid=101 bookid=1
```

![image](https://user-images.githubusercontent.com/84724396/123270466-e4e58a80-d53a-11eb-8e48-694dd61f35c4.png)


2.6.configmap 삭제 후 app 서비스 재시작 -->Fail

```
kubectl delete configmap --all -n rbook
```
![image](https://user-images.githubusercontent.com/84724396/123270712-1fe7be00-d53b-11eb-851b-55f753801160.png)


```
kubectl get pod/rent-85c54dd5b-gzjrb -n rbook -o yaml | kubectl replace --force -f-
--> CreateContainerConfigError 발생 
```

![image](https://user-images.githubusercontent.com/84724396/123271000-5ae9f180-d53b-11eb-9836-fb974423fa6a.png)



### 3. 동기식 호출 / Circuit Breaker / 장애격리

3.1.서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 대여서비스(rent)--> book 서비스의 연결을 RESTful Request/Response 로 연동하여 구현하였고, Book 서비스 요청이 과도할 경우 CB 를 통하여 장애격리를 격리한다.

3.2.동기 호출 주체인 Rent 서비스의 application.yml 파일에 Hystrix 설정

Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

```
rent/src/main/resources/application.yml 파일

feign:
  hystrix:
    enabled: true
    
hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```

![image](https://user-images.githubusercontent.com/84724396/123293680-2a13b780-d54f-11eb-80b8-88c9aaf36442.png)


3.3.siege 툴 사용법:

```
 1)siege가 생성되어 있지 않으면:
 kubectl run siege --image=apexacme/siege-nginx -n rbook
 
 2)siege 들어가기:
 kubectl exec -it pod/siege-d484db9c-ln4r4 -c siege -n rbook -- /bin/bash
 
 3)siege 종료:
 Ctrl + C -> exit
```

3.4.부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인: 동시사용자 100명, 20초 동안 실시

```
siege -c100 -t20S -r10 -v --content-type "application/json" 'http://20.194.57.130:8080/rents POST {"userid": "201", "bookid": "7" }'

```
3.5부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 book 에서 처리되면서 다시 rent 를 받기 시작 

![image](https://user-images.githubusercontent.com/84724396/123355628-04170300-d5a1-11eb-8f9d-45de4a446673.png)

![image](https://user-images.githubusercontent.com/84724396/123355681-23159500-d5a1-11eb-8fe4-7ea3fabbac46.png)


### 4. 오토스케일 아웃

4.1.book 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다.

```
book > deployment.yml 설정
```
![image](https://user-images.githubusercontent.com/73699193/98187434-44fbd200-1f54-11eb-9859-daf26f812788.png)

```
kubectl autoscale deploy book --min=1 --max=10 --cpu-percent=15 -n rbook
```
![image](https://user-images.githubusercontent.com/84724396/123357124-fd3dbf80-d5a3-11eb-8880-5d3c246b13df.png)


4.2.CB 에서 했던 방식대로 부하를 걸어 준다 : 동시사용자 200명, 120초 동안 실시

```
kubectl exec -it pod/siege-d484db9c-ln4r4 -c siege -n rbook -- /bin/bash
siege -c200 -t120S -r10 -v --content-type "application/json" 'http://20.194.57.130:8080/rents POST {"userid": "201", "bookid": "7" }'
```
![image](https://user-images.githubusercontent.com/84724396/123365263-e8b3f400-d5b0-11eb-9445-75ee5ea96259.png)

4.3.오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:

```
kubectl get deploy book -w -n rbook
```
- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다. max=10 
- 부하를 줄이니 늘어난 스케일이 점점 줄어들었다.

![image](https://user-images.githubusercontent.com/84724396/123364797-0c2a6f00-d5b0-11eb-8d4a-7cf9c3ec0a5f.png)


### 5. Zero-downtime deploy (readiness probe)

5.1. deployment_readyness.yml에 readiness 옵션을 제거하고 배포

```
kubectl create -f ./kubernetes/deployment_readyness.yml -n rbook
kubectl create -f ./kubernetes/service.yaml -n rbook
```

![image](https://user-images.githubusercontent.com/84724396/123377084-7e0db300-d5c6-11eb-81e9-f4a8262c836b.png)

5.2.새로운 버전의 이미지로 교체

```
az acr build --registry skccrjh2 --image skccrjh2.azurecr.io/billing:v8 .
kubectl set image deploy billing billing=skccrjh2.azurecr.io/billing:v8 -n rbook
```

5.3.배포 중 서비스 요청처리 실패

![image](https://user-images.githubusercontent.com/84724396/123377034-6cc4a680-d5c6-11eb-81e3-17a659d3ddeb.png)


5.4 deployment.yml에 readiness 옵션을 적용해서 배포

- readiness적용된 deployment.yml 적용

![image](https://user-images.githubusercontent.com/84724396/123374613-56b4e700-d5c2-11eb-908d-f4f1194de3c8.png)

```
kubectl create -f ./kubernetes/deployment.yml -n rbook
kubectl create -f ./kubernetes/service.yaml -n rbook
```

5.5.새로운 버전의 이미지로 교체

```
az acr build --registry skccrjh2 --image skccrjh2.azurecr.io/billing:v9 .
kubectl set image deploy billing billing=skccrjh2.azurecr.io/billing:v9 -n rbook
```

5.3.기존 버전과 새 버전의 billing pod 공존 중

![image](https://user-images.githubusercontent.com/84724396/123375164-46e9d280-d5c3-11eb-8d65-72d605b24661.png)

- Availability: 100.00 % 확인

![image](https://user-images.githubusercontent.com/84724396/123375245-684abe80-d5c3-11eb-8b00-1a48209777f0.png)


### 6. Self-healing (Liveness Probe)

6.1.billing 서비스 정상 확인

![image](https://user-images.githubusercontent.com/84724396/123284221-0cdaeb00-d547-11eb-889f-8551999656a4.png)

6.2.billing의 deployment.yml 에 Liveness Probe 옵션 추가

```
cd ~/rbook/billing/kubernetes
vi deployment.yml

(아래 설정 변경)
 livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8087
            initialDelaySeconds: 3
            periodSeconds: 5
```
![image](https://user-images.githubusercontent.com/84724396/123288765-d30be380-d54a-11eb-8b7e-e3875e6250e9.png)


6.3.deployment.yml 적용 후 billing pod에 liveness가 적용된 부분 확인

```
kubectl apply -f kubernetes/deployment.yml -n rbook
kubectl describe deploy billing -n rbook 
```

![image](https://user-images.githubusercontent.com/84724396/123287323-9f7c8980-d549-11eb-8d16-39fc54daea14.png)

6.4.billin 서비스의 liveness가 발동되어 5번 retry 시도 한 부분 확인

![image](https://user-images.githubusercontent.com/84724396/123286296-c25a6e00-d548-11eb-98f9-6e4a6438b70f.png)



