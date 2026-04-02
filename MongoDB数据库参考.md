user集合：
{
  "_id": {
    "$oid": "69c8c70078e011f98d25d92a"
  },
  "account": "test3",
  "password": "12345",
  "create_at": {
    "$date": "2026-01-12T10:30:00.000Z"
  }
}

repository集合：
{
  "_id": {b
    "$oid": "69cc8db526a2d001d1dc6e56"
  },
  "userId": "69ca0c68d6cab62b14596190",
  "name": "测试表格.xlsx",
  "url": "/uploads/41af6b57-04a8-4105-8fa8-5e8e9e472868.xlsx",
  "_class": "com.example.demo.model.Repository"
}

message集合：
{
  "_id": {
    "$oid": "69ca6f86b122e338c197b860"
  },
  "communicationId": "69ca6e677744286c8c3af4e3",
  "status": 1,
  "content": "你是谁",
  "create_at": {
    "$date": "2026-03-30T12:41:42.212Z"
  },
  "_class": "com.example.demo.model.Message"
}

data集合：
{
  "_id": {
    "$oid": "69cb39fb16c4e5b939ca16d7"
  },
  "keyword": "架构",
  "context_text": "架构实际上就是指人们根据自己对世界的认识，为解决某个问题，主动地、有目的地去识别问题，并进行分解、合并，解决这个问题的实践活动。"
}

communication集合：
{
  "_id": {
    "$oid": "69ca6e677744286c8c3af4e3"
  },
  "userId": "69ca0c68d6cab62b14596190",
  "name": "测试1",
  "create_at": {
    "$date": "2026-03-30T12:36:54.998Z"
  },
  "_class": "com.example.demo.model.Communication"
}