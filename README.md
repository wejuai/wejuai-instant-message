# wejuai-instant-message
即时消息服务
### 连接path
 - /webSocket/{当前用户accountsId}
 - accountsId从用户基础信息接口获得
## 消息体
- String recipient  接收人userId
- String message    文本消息内容
- String ossKey     如果是传的图片、语音、视频这里是文件的ossKey
- MediaType mediaType   这里是媒体文件的类型
  - IMAGE
  - VIDEO 
  - AUDIO

### 注意
1. 如果是媒体类型，不要带text，带了也无效，会被替换掉
2. 消息体是json类型

### 维持连接
消息体内直接传入关键字字符串`heart`，非json

### 配置项
- bootstrap.yml配置config-server信息
- spring配置还是在config-server中查看详情
- `.gitlab-ci.yml`配置gitlab runner执行过程

### note
1. ChatUserRecord 和 SendMessage实体记录记录的均为userId,但是前端连接使用的是accountsId注意转换
2. 发送消息是根据accountsId和sessionId的双向关联获取到sessionId再获取session
3. 注意发送人和接收人的session别反了

### 本地运行
1. 配置项以及其中的第三方服务开通
2. gradle build，其中github的仓库必须使用key才可以下载，需要在个人文件夹下的`.gradle/gradle.properties`中添加对应的`key=value`方式配置，如果不行，就去下载对应仓库的代码本地install一下
3. 启动配置项中的数据库
4. 运行Application.java的main方法

### docker build以及运行
- 运行gradle中的docker build task
- 如果配置了其中的第三方仓库可以运行docker push，会先build再push
- 运行方式 docker run {image name:tag}，默认是运行的profile为dev，可以通过环境变量的方式修改，默认启动配置参数在Dockerfile中