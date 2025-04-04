db.auth("postgres", "postgres");  // Авторизация администратора

db = db.getSiblingDB("cloudstorage");  // Переход в нужную базу данных

db.createUser({
    user: "user",
    pwd: "password",
    roles: [{
        role: "dbOwner",
        db: "cloudstorage",
    }],
});
