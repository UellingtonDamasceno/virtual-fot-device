# virtual-fot-device
Um despositivo virtual capaz simular sensores e de se comunicar através de um 
broker MQTT utilizando algumas primitivas do protocolo TATU e ExtendedTATU.

## Sumário

# Tópicos 
Este dispositivo virtual recebe requisições no tópico ``dev/DEVICE_ID``
responde no tópico ``dev/DEVICE_ID/RES``.

  > Atenção: Para o MQTT os tópico ``my/topic`` e ``my/topic/`` são distintos
  > por isso CUIDADO quando for se inscrever ou publicar.

# Metodos compatíveis

| Metodo  | Compatível |
|---------|------------|
| GET     | SIM        |
| FLOW    | SIM        |
| EVT     | NÃO        |
| POST    | SIM        |
| SET     | PARCIAL    |
| CONNECT | SIM        |

# Exemplos de request compatíveis
### GET
    GET VALUE sensorName
    
### FLOW
    FLOW VALUE sensorName {"publish_time": int, "collect_time":int}
       
  > Para interromper o fluxo de dados faça uma nova requisição utilizando um valor
  > de ``publish_time`` ou ``collect_time`` menor ou igual a zero. 

### SET
  Atualmente este dispositivo somente é capaz de responder a seguinte solicitação
  do tipo SET.
  
  SET VALUE mqttBroker {id:String, url:String, port:int, user:String, password:String}

### CONNECT

As solicitações do tipo ``CONNECT`` devem ser enviadas para o tópico ``dev/CONNECTIONS``
e são respondidas no tópico ``dev/CONNECTIONS/RES``.

    CONNECT VALUE BROKER {"HEADER":{"NAME":String}, "TIME_OUT":Double}
  
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
