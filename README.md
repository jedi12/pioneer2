# Система управления документами "Пионер"

#### Простая и гибкая система управления документами.  

Под *документом* понимается любая сущность, созданная на основе [шаблона](http://pioneersystem.ucoz.ru/index/shablon_dokumenta/0-14 "Шаблон") (документ, заявка, задача, запрос, заказ и т.д.).  
Под *управлением* понимается поэтапное "движение" документов между группами пользователей в соответствии с установленным для документа [маршрутом](http://pioneersystem.ucoz.ru/index/marshrut/0-13 "Маршрут документа"). Каждому шаблону назначается свой маршрут. При создании документу назначается маршрут шаблона, на основе которого он создается. При необходимости маршрут документа может быть изменен автоматически при создании нового документа на основе введенных пользователем данных или вручную пользователем, участвующем в процессах согласования или исполнения (при наличии соответствующих прав).  
Интерфейс системы оптимизирован для работы с большим количеством документов и пользователей.  
  
Перед началом работы, пользователь с административной ролью выполняет все необходимые настройки (создает набор шаблонов заявок, задает для них маршруты), а пользователи на основе этих шаблонов «в два клика» создают документы и отправляют их на обработку по заданному маршруту (на согласование, исполнение и т.д.).

### Требования
* JDK 8
* Apache Tomcat 8.5+
* H2 database (также возможна работа с Oracle 10g+ или PostgreSQL 9+)
