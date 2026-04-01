启动后端服务：
```
mvn spring-boot:run
```
启动前端服务：
```
npm run serve
```
推送GitHub：
```
(1) 可选：先看状态是否有未保存的改动
git status
(2) 暂存改动
git add .
(3) 提交说明
git commit -m "引号里面写入这个修改的描述"
(4) 推送到远程主分支
git push origin master
```
拉取GitHub：
```
(1) 可选：先看状态，保持工作区干净
git status
(2) 同步远程引用，不修改本地代码
git fetch origin
(3) 合并远程最新分支到当前分支（如在 master 上）
git pull origin master
(4) 当你要让本地强制与远程一致（会覆盖本地改动）
# git reset --hard origin/master
```

> 小提示：遇到 "502" 错误，多半是网络/代理或GitHub服务问题，稍后重试或改用SSH。