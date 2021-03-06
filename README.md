## Apache KAFKA

### Peculiaridades
* Diferente do Rabbitmq, o kafka não envia a mensagem ao consumer, ele que tem de ir buscar
* Diferente do RabbitMq, quando um consumer lê uma mensagem ela não some da fila.

### Consumer 
São os clientes que leem de um determinado tópico, o ideal é que para cada consumidor exista uma partição.   
porém, pode ser que um consumidor leia de mais de uma partição.   


### Broker 
Cada broker é uma maquina diferente em que está instalado o kafka  
Pode ter uma ou mais partições;  
O Broker "assina" um ou mais tópicos ?


### Tópicos   
Todas as mensagens são publicadas em um tópico, este tópico por sua vez pode estar distribuido em N partições
Uma cada partição terá o seu broker e estará fisicamente em uma máquina do cluster
Por exemplo:   
imagine que você tem um tópico que está sendo publicado duas partições, envia 2000 mensagens para este tópico, o kafka então irá distribuir essas 2000 mensagnes 1000 para a partição 1 e 1000 para a partição 2.

### Partições
São as várias "caixinhas" onde as mensagens serão gravadas, normalmente cada partição fica em uma máquina diferente, garantindo a escalabilidade.

### Como garantir a ordem de entrega   
**Só é possível garantir a ordem das mensagens dentro da mesma partição**

### Replicator factor  
Replicator factor - permique que tenha cópias de cada partição em brokers diferentes, garantindo assim a resiliência 

### partições do tipo leaders e partições do tipo followers 
Mesmo que o replication factor for 2, (duas cópias de cada partição por broker), uma das partições do broker será a lider e as outras serão as followers. Caso caia um dos brokers ele elegerá o lider que caiu em outra partição, está por sua vez passa a contar com 2 lideres (um para cada partição)



### Anatomia das mensagens
As mensagens, são posicionadas numa fila e sua posição é chamada de OffSet. 
As mensagnes são compostas por quatro partes :  
**Offset 0**
* Headers &rarr; de livre uso, pode armazenar informações que serão utilizadas pelos consumidores
* Key &rarr; quando eu quero garantir a ordem eu uso a mesma key, dessa forma todas as mensagens com a mesma key cairão na mesma partição.
* Value &rarr; 
* Timestamp &rarr; 


### Producer: Garantia de entrega 
* Ack 0 (None) &rarr; NÃO garante que a mensagem foi entregue ao broker (caso posição do carro do motorista UBER)
    * Pode processar muito mais mensagens, porem, sem a certeza de que foram entregues

* Ack 1 (Leader) &rarr; garante que a mensagem foi entregue ao broker, porém, NÃO garante que esta mensagem foi replicada a outros brokers
    * É um pouco mais rápido, que a Ack 0, mas se der um problema na máquina antes da replicação ocorrer a mensagem será perdida;

* Ack -1 (ALL) &rarr; garante que a mensagem foi entregue e foi replicada a todos os outros brokers 
    * Só envia a confirmação após toda a replicação for concluida
    * muito seguro, porém, é o modo mais lento de trabalho 

### outros modelos de garantia de entrega 
* At most once &rarr; Melhor performance, mas pode perder mensagens 
* At least once &rarr; (pelo menos uma vez a mensagem será entregue), performance moderada mas pode duplicar mensagens 
* Exactly once (exatamente uma vez) &rarr; Pior performance, mas não perde as mensagens 

### Producer idepotente  
**Problema:**   
Se ocorrer algo durante o processo de publicação da mensagem, o produtor pode enviar novamente e isso causar duplicidade da mensagem  
* Modo enable true &rarr; o kafka se vira e nunca você terá mensagens fora de ordem e nem duplicadas
* Modo enable false &rarr;  o tratamento de mensagens duplicadas tem de ser feito no _consumer_
***IMPORTANTE***: Para produtor **idempotent**  ser habilitado o _Ack_ tem de ser -1 (ALL)

### Consumer e Consumer groups 
* Os Consumers que estão no mesmo grupo leem as mensagens de forma distribuida de varias partiçoes 
* Se o consumer não pertence a grupo nenhum ele irá consumir de todas as partições
* Não tem como 2 consumidores que estão no mesmo grupo lerem a mesma partição, neste caso cada consumidor lê uma partição
* se tiver mais 4 consumidores em um determinado grupo e tiver apenas 3 partições, um dos consumidores ficará IDLE (parado)
* Melhor solução então é ter a mesma quantidade de partições para a mesma quantidade de consumer no mesmo grupo 


### COMANDOS DO KAFKA 
**Criar um topico:**   
```kafka-topics --create --topic=<nome-do-topico> --bootstrap-server=<ip-do-servidor-kafka:9092> --partitions=<numero-de-partições> --replication-factor=<numero-de-replicas>```   
**Importante** 
_bootstrap-server_ e _replication\_factor_ são requeridos e i _replication\_factor_ não pode ser maior qua a quantidade de brokers.  
**nesta hora é que as partições são criadas**   


**Listar os topicos existentes:**   
```kafka-topics --list --bootstrap-server=<ip-do-servidor>```   

**Descreve o topico, suas partiçoes e replicações :**   
```kafka-topics --topic=<nome-do-topico> --describe --bootstrap-server=<ip-do-servidor>```   


**kafka-console-consumer**  
**Consumir topicos  :**   
```kafka-console-consumer --topic=<nome-do-topico> --bootstrap-server=<ip-do-servidor> [--from-beginning] [--group=<nome-do-grupo>]```   
**Importante**  
_from\_beginning_ &rarr; le as mensagens desde o inicio (não garante a orderm pois lerá de todas as partições)    
_--group_ &rarr; especifica o grupo de consumers que este deverá fazer parte, desta forma a "carga" será distribuida entre estes consumers.   

quando não especificado o group, todas as mensagnes vão chegar para todos consumers "sem grupo", criando uma espécie de fanout. Utiliza-se assim quando 
varios consumidores terão de fazer processamentos diferentes com a mesma mensagem.   
para distribuir a carga, ou seja um consumer ler uma mensagem e processar e outro ler outra mensagem e processar, é necessário colocar os referidos consumers no mesmo grupo.

![exemplo](./imagens/fanout.png "Exemplo")

**kafka-console-producer**  
**Produzir mensagens em um topico  :**   
```kafka-console-producer --topic=<nome-do-topico> --bootstrap-server=<ip-do-servidor> ```   

***Observações**  
* Se você tiver 2 ou mais consumers conectados ao mesmo tópico, ambos lerão todas as mensagens publicadas neste tópico, independente da partição 
* Quando você tiver 2 ou mais consumers e quiser distribuir a carga (que cada um leia de uma partição), deve colocá-los no mesmo grupo 


**kafka-consumer-groups**  
**Descrever como estão organizados as partiçoes/offsets de um determinado grupo**   
```kafka-consumer-groups --bootstrap-server=<ip-do-servidor> -group=<nome-do-grupo> --describe```   
* da pra ver qual partição cada consumer do grupo está lendo  
* da pra ver qual o current off-set, quantas mensagens tem no total e quantas ainda faltam para ser lidas (lag)   

### Confluent control-center  
http://localhost:9021   

### schema registry   
Permite que a gente defina o formato de uma mensagem (utilizando o avro)  


## Extensões vscode 
* streetsidesoftware.avro  


## kafka landoop  
http://localhost:3030/  
http://localhost:3030/kafka-topics-ui/#/cluster/fast-data-dev/topic/n/topico-teste/data  


## Para consumir  
```docker exec -it kafka_kafka-cluster_1 bash ```  
``` kafka-avro-console-consumer --topic topico-teste --bootstrap-server=localhost:9092 --from-beginning --property schema.registry.url=http://127.0.0.1:8081 ```   


## Links interessantes 
https://www.tutorialspoint.com/apache_kafka/index.htm
https://docs.spring.io/spring-kafka/reference/html/#with-java-configuration-no-spring-boot
https://avro.apache.org/docs/current/gettingstartedjava.html
https://programadev.com.br/kafka-producer-avro/   
https://github.com/guilhermegarcia86/kafka-series/tree/avro/register  
https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md &rarr; parâmetros de configuração
Global configuration (todos os parâmetros) &rarr https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md
https://betterprogramming.pub/adding-schema-registry-to-kafka-in-your-local-docker-environment-49ada28c8a9b
https://docs.confluent.io/platform/current/schema-registry/schema_registry_tutorial.html
https://qastack.com.br/programming/38024514/understanding-kafka-topics-and-partitions  
https://medium.com/engenharia-arquivei/subindo-um-cluster-de-kafka-ffec258b1175#:~:text=Rodar%20o%20kafka%20em%20um,adequadamente%2C%20e%20iniciar%20o%20servi%C3%A7o.   


### para programar em outras linguagens sem ser java necessita biblioteca librdkafka
https://github.com/edenhill/librdkafka &rarr; biblioteca rdkafka utilizada para comunicação com outras linguagens (exceto java, já que o kafka é nativo java)

