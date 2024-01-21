# 管理平台接口 V1.0 说明文档

[TOC]

## 一、全局说明

** 本接口文档对SaaS管理后台接口操作，相关操作、响应和错误码进行定义。**

### 1.Cookie说明
登录判断标准：不论是否登录成功都会在请求响应的cookie中种一个`key=“t”`的ticket，如果登录成功，会将此ticket与用户的token绑定，`所以前端需要统一处理cookie中的“t”并每次请求时都携带并传递给服务端。`

### 2.参数说明
入参以及返回数据均为Json格式。所有提交以及返回接收的变量均使用驼峰形式。

### 3.返回数据结构
返回数据由三部分构成：
  - code： 响应状态码（返回状态码说明见文档末尾部分）
  - msg：后端返回的提示信息
  - data：有返回数据的接口，此字段为对应数据，具体结构见具体接口
    
```json
{
    "code" : 0, //0为请求成功，请求失败显示对应的错误码
    "msg"  : "", //提示信息
    "data" : {} //有返回数据时，data内为返回数据
}
```
### 4.地址：
cn：   admin-server.bhop.svc.cluster.local:7501
---------------

## 二、详细接口说明
## 1.登录
**接口地址**`“https://server:port/api/v1/user/login”`  
**请求方式**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|------:|
|username       |String     |用户名        |Y |
|password       |String     |密码     |Y |

**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "" //失败时错误信息
}
```

## 2.登出
   **接口地址** `“https://server:port/api/v1/user/logout”`  
   **请求方式** `GET`  
   
   
   |参数 |类型 |说明 | 是否必填 |
   |-------|------:|------:|------:|
   
   **json返回数据**
   ```json
   {
       "code" : 0, //全局统一，0为请求成功，1为请求失败
       "msg"  : "" //失败时错误信息
   }
   ```

### 3.创建交易所
**接口地址：**`“https://server:port/api/v1/exchange/create_exchange”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|------:|
|exchangeName       |String     |交易所简称       |Y |
|saasFeeRate       |bigDecimal     |saas费率      |Y |
|company      |String     |公司名称 | N |
|email      |String     |邮箱账号   |Y |
|contactName      |String     |联系人        | Y|
|contactTelephone | String     |联系电话      | Y|
|remark           | String     | 其它信息     |N|


**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "" //失败时错误信息
}
```

### 4.编辑交易所
**接口地址：**`“https://server:port/api/v1/exchange/edit_exchange”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|------:|
|saasFeeRate       |bigDecimal     |saas费率      |Y |
|company      |String     |公司名称 | N |
|contactName      |String     |联系人        | Y|
|contactTelephone | String     |联系电话      | Y|
|remark           | String     | 其它信息     |N|


**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "" //失败时错误信息
}
```

### 5.交易所列表
**接口地址：**`“https://server:port/api/v1/exchange/query_exchanges”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|-----:|
|current        |  int |当前页|Y|
|pageSize        |  int |每页条数|Y|
|exchangeName        |  string |待查询的交易所名称|N|
|exchangeId        |  Long |待查询的交易所id|N|

**json返回数据**
```json
{
    "code": 0,
    "msg": null,
    "data": {
        "current": 1,
        "pageSize": 5,
        "total": 1, //总条数
        "list": [
            {
                "id": 82848558554288128,
                "exchangeId": 617211120586927, //交易所id
                "exchangeName": "test-exchange-8", //交易所名称
                "saasFeeRate": 0.0001,//saas费率
                "company": "sdfkiekdi",//公司名称
                "email": "li@132.com",//email
                "contactName": "cool man",//联系人名称
                "contactTelephone": "1590999",//联系人电话
                "status": 0, // 0-禁用 1-启用
                "payEarnest": 0 //是否交纳保证金
            }
        ]
    }
}
```

### 6.交易所详情信息
**接口地址：**`“https://server:port/api/v1/exchange/{id}”`  
**请求方式：**`GET`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|-----:|
|id        |  long |id|Y|

**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "", //失败时错误信息
    "data" : {
                 "id": 82848558554288128,
                 "exchangeId": 617211120586927, //交易所id
                 "exchangeName": "test-exchange-8", //交易所名称
                 "saasFeeRate": 0.0001,//saas费率
                 "company": "sdfkiekdi",//公司名称
                 "email": "li@132.com",//email
                 "contactName": "cool man",//联系人名称
                 "contactTelephone": "1590999",//联系人电话
                 "payEarnest": 0 //是否交纳保证金
             }
}
```

### 7.启动交易所
**接口地址：**`“https://server:port/api/v1/exchange/enable_exchange”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|-----:|
|id        |  long |id|Y|
|remark        |  String |备注|Y|
**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : ""//失败时错误信息
}
```

### 8.禁用交易所
**接口地址：**`“https://server:port/api/v1/exchange/disable_exchange”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|-----:|
|id        |  long |id|Y|
|remark        |  String |备注|Y|
**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : ""//失败时错误信息
}
```











### 9.创建券商
**接口地址：**`“https://server:port/api/v1/broker”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|------:|
|name       |String     |券商简称       |Y |
|saasFeeRate       |bigDecimal     |saas费率      |Y |
|company      |String     |公司名称 | N |
|email      |String     |邮箱账号   |Y |
|contact      |String     |联系人        | Y|
|phone | String     |联系电话      | Y|
|basicInfo           | String     | 基础信息     |N|


**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "" //失败时错误信息
}
```

### 10.券商列表
**接口地址：**`“https://server:port/api/v1/broker/query_broker”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|-----:|
|current        |  int |当前页|Y|
|pageSize        |  int |每页条数|Y|
|brokerName        |  string |待查询的券商名称|N|
|brokerId        |  Long |待查询的券商id|N|

**json返回数据**
```json
{
    "code": 0,
    "msg": null,
    "data": {
        "current": 1,
        "pageSize": 5,
        "total": 1, //总条数
        "list": [
            {
             "id": 1,
             "brokerId": 100, //机构ID，由平台生成
             "name": "机构简称",
             "company": "机构全名",
             "email": "test@bhex.com",  //邮箱
             "phone": "1508260828",  //手机号
             "host": "broker.com",  //域名
             "contact": "联系人",  //联系人
             "email": "t",  //邮箱
             "basicInfo": "基础信息",  //基础信息
             "enabled": true  //是否启用
            }
        ]
    }
}
```

### 11.获取Broker保证金地址
**接口地址：**`“https://server:port/api/v1/broker/earnest_address”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |
|-------|------:|------:|
|id     |Long       |机构主键ID     |

**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "", //失败时错误信息
    "data" : {
              "earnestAddress": "XXXOOOXXXOOXXOO"  //保证金地址
    }
}
```

### 12.Broker 禁用券商功能
**接口地址：**`“https://server:port/api/v1/broker/enable”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |
|-------|------:|------:|
|id     |Long       |机构主键ID，非brokerId      |

**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "" //失败时错误信息
}
```

### 13.Broker 启用券商功能
**接口地址：**`“https://server:port/api/v1/broker/disable”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |
|-------|------:|------:|
|id     |Long       |机构主键ID，非brokerId      |

**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "" //失败时错误信息
}
```

### 14.通过ID获取Broker信息
**接口地址：**`“https://server:port/api/v1/broker/find”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |
|-------|------:|------:|
|id     |Long       |机构主键ID，非brokerId      |

**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "", //失败时错误信息
    "data" : {
             "id": 1,
             "brokerId": 100, //机构ID，由平台生成
             "name": "机构简称",
             "company": "机构全名",
             "email": "test@bhex.com",  //邮箱
             "phone": "1508260828",  //手机号
             "host": "broker.com",  //域名
             "contact": "联系人",  //联系人
             "email": "t",  //邮箱
             "basicInfo": "基础信息",  //基础信息
             "enabled": true  //是否启用
    }
}
```

### 15.通过ID修改Broker信息
**接口地址：**`“https://server:port/api/v1/broker/edit”`  
**请求方式：**`POST`  


|参数 |类型 |说明 |是否必填 |
|-------|------:|------:|------:|
|id     |Long       |机构主键ID，非brokerId      |Y |
|saasFeeRate       |bigDecimal     |saas费率      |Y |
|company      |String     |公司名称 | N |
|contact      |String     |联系人        | Y|
|phone | String     |联系电话      | Y|
|basicInfo           | String     | 基础信息     |N|

**json返回数据**
```json
{
    "code" : 0, //全局统一，0为请求成功，1为请求失败
    "msg"  : "" //失败时错误信息
}
```


## 三、全局返回码

每次调用接口时，可能获得正确或错误的返回码，开发者可以根据返回码信息调试接口，排查错误。

|返回码 |说明 |
|-------|:------|
|0      |请求成功       |
|1      |未知系统错误      |
|16xx   |业务错误，细节待完善 |