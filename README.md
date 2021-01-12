# Virtual-fot-device
Um despositivo virtual capaz de simular sensores e de se comunicar através de um 
broker MQTT utilizando algumas das primitivas do protocolo TATU e ExtendedTATU.

## Sumário
- [Inicialização](#inicialização)
- [Tópicos](#tópicos)
- [Métodos compatíveis](#métodos-compatíveis)
- [Exemplo de request](#exemplos-de-request)
  - [GET](#get)
  - [FLOW](#flow)
  - [SET](#set)
  - [CONNECT](#connect)

# Inicialização
Se nenhum argumento for informado durante a incialização o dispositivo virtual irá se conectar
ao ``localhost`` utilizando a porta ``1883``.Todavia, é possível alterar essas informações
passando os seguintes paramentros de inicialização:

| ARG | Significado | Descrição| Padrão |
|-----|-------------|----------|--------|
|**-di**| Device ID | Define o nome utilzado pelo dispositivo na identificação da conexão MQTT| Aleatório |
|**-bi**| Broker IP | Define o IP do broker MQTT | localhost|
|**-pt**| Port     | Define a porta da conexão MQTT | 1883 |
|**-us**| Username  | Define o usuário da conexão | karaf |
|**-pw**| Password     | Define a senha de conexão do broker | karaf |

  > Atenção: Não pode haver 2 dispositivos com o mesmo device ID em um mesmo broker.

# Tópicos 
Este dispositivo virtual recebe requisições no tópico ``dev/DEVICE_ID`` e
responde no ``dev/DEVICE_ID/RES``.

  > Atenção: No protocolo MQTT os tópicos ``my/topic`` e ``my/topic/`` são distintos.
  > Por isso, CUIDADO quando for se inscrever ou publicar.

# Métodos compatíveis

| Método  | Compatível |
|---------|------------|
| GET     | SIM        |
| FLOW    | SIM        |
| EVT     | NÃO        |
| POST    | SIM        |
| SET     | PARCIAL    |
| CONNECT | SIM        |

# Exemplos de request
### GET
    GET VALUE sensorName
    
### FLOW
    FLOW VALUE sensorName {"publish_time": int, "collect_time":int}
       
  > Para interromper o fluxo de dados, faça uma nova requisição utilizando um valor
  > de ``publish_time`` ou ``collect_time`` menor ou igual a zero. 

### SET
Atualmente este dispositivo somente é capaz de responder a seguinte solicitação
do tipo SET.
  
    SET VALUE mqttBroker {"id":"String", "url":"String", "port":"int", "user":"String", "password":"String"}

### CONNECT

As solicitações do tipo ``CONNECT`` devem ser enviadas para o tópico ``dev/CONNECTIONS``
e são respondidas no tópico ``dev/CONNECTIONS/RES``.

    CONNECT VALUE BROKER {"HEADER":{"NAME":"String"}, "TIME_OUT":"Double"}
  
O ``TIME_OUT`` é utilizado para informar ao getaway quanto tempo o device está disposto
a esperar resposta se pode ou não efetuar a transição de getaways. 
  
O device ao enviar uma mensgem do tipo ``CONNECT`` espera receber uma mensagem do tipo 
``CONNACK`` com o seguinte formato:

```json
    {
      "CODE":"POST",
      "METHOD":"CONNACK",
      "HEADER":{"NAME":"String"},
      "BODY":{"NEW_NAME":"String", "CAN_CONNECT":"Boolean"}
    }
```
