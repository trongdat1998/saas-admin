# Admin panel for BlueHelix

## DEV

- run docker mysql in localhost

```bash
docker run -d -p 127.0.0.1:3306:3306 --name=dev-mysql -e MYSQL\_ROOT\_PASSWORD=root mysql
```

- init db schema

```bash
mysql -h127.0.0.1 -uroot -proot -Dguns < server/sql/guns.sql
```

- start test

## DEPLOY

## 添加新功能

1. 根据新的表代码生成相应entity, repostory 
1. 复制controller(/io/bhex/admin/modular/bhex/controller),并修改
1. 复制view(/server/src/main/webapp/WEB-INF/view/exchange)，并修改
1. 复制js(/server/src/main/webapp/static/modular/exchange)并修改

## 自动生成 

@todo

## 修复数据库连接

分别修改项目 server与generator下的application.yml配置 rest暂时没用到

