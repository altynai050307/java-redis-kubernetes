# Java + Redis on Kubernetes

Бұл жоба Java (Spring Boot) қолданбасын Redis-пен бірге Kubernetes-ке орналастыруға арналған.

## Құрылымы

- `src/` — Java қолданбасының бастапқы коды
- `Dockerfile` — контейнер құрастыру
- `k8s/00-redis.yaml` — Redis Deployment және Service
- `k8s/10-java-stable.yaml` — Java stable Deployment және Service
- `k8s/20-java-canary.yaml` — қарапайым Canary Deployment
- `k8s/30-network-policy.yaml` — Java подтарына Redis-ке қосылуға рұқсат

Маңызды label-дер:
- Redis pod: `app: redis`
- Java stable және canary pod: `app: java-redis-app`

## 1. Алдын ала қажет

Docker, kubectl және жұмыс істеп тұрған Kubernetes кластері қажет. `kubectl` дұрыс кластерге қосылғанын тексеріңіз:

```bash
kubectl cluster-info
```

## 2. Docker image құрастыру

Жобаның түбірлік бумасында:

```bash
docker build -t java-redis-app:1.0 .
```

Егер **Minikube** қолдансаңыз, image-ті кластерге жүктеңіз:

```bash
minikube image load java-redis-app:1.0
```

Егер **Kind** қолдансаңыз:

```bash
kind load docker-image java-redis-app:1.0
```

Басқа кластерде image-ті registry-ге push жасап, YAML файлдарындағы `image: java-redis-app:1.0` жолын registry image атауымен ауыстырыңыз.

## 3. Қолданбаны орналастыру

Жоба түбірінде:

```bash
kubectl apply -f k8s/
```

Қолданылуы тиіс негізгі команданың баламасы — `kubectl apply -f ...`; бұл жобада нақты бума `k8s/`.

## 4. Тексеру

```bash
kubectl get pods -o wide
kubectl get svc
kubectl get networkpolicy
kubectl get pods -l app=redis --show-labels
kubectl get pods -l app=java-redis-app --show-labels
```

Барлық подтар `Running` және `READY` күйіне келгенше күтіңіз:

```bash
kubectl rollout status deployment/redis
kubectl rollout status deployment/java-redis-app
kubectl rollout status deployment/java-redis-canary
```

## 5. API-ді тексеру

```bash
kubectl port-forward svc/java-redis-app 8080:80
```

Басқа терминалда:

```bash
curl http://localhost:8080/
```

Әр сұрауда Redis-тегі `visits` есептегіші өседі. Жауапта `version` мәні `stable` немесе `canary` болып көрінеді.

## 6. Canary үлесін өзгерту

Service `app: java-redis-app` бойынша stable және canary подтарын бірге таңдайды. Мысалы, canary үлесін арттыру:

```bash
kubectl scale deployment/java-redis-canary --replicas=2
```

Canary-ді уақытша өшіру:

```bash
kubectl scale deployment/java-redis-canary --replicas=0
```

Бұл — Kubernetes Service арқылы іске асатын қарапайым, кездейсоқ үлесті Canary. Бұл автоматты traffic-splitting контроллері (мысалы, Flagger/Istio) емес.

## Ескерту

NetworkPolicy жұмыс істеуі үшін кластердегі CNI NetworkPolicy-ді қолдауы керек (мысалы, Calico немесе Cilium). Кейбір жергілікті кластерлерде саясат қолдауы қосылмаған болуы мүмкін.
