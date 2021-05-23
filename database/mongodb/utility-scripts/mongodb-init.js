// JavaScript for adding a user to MongoDB with read/write permissions to the appropriate databases

const MONGO_INITDB_ROOT_USERNAME = "MONGO_INITDB_ROOT_USERNAME";
const MONGO_INITDB_ROOT_PASSWORD = "MONGO_INITDB_ROOT_PASSWORD";
const MONGO_USERNAME = "MONGO_USERNAME";
const MONGO_PASSWORD = "MONGO_PASSWORD";

const MONGO_ADMIN_DATABASE = "admin"
const MONGO_INITDB_DATABASE = "MONGO_INITDB_DATABASE";
const MONGO_ADDITIONAL_DATABASES = "MONGO_ADDITIONAL_DATABASES";
const DATABASE_SEPARATOR = ",";
const DATABASE_PERMISSIONS = "readWrite";

const STRING_ROLES = "roles";
const STRING_DB = "db";

// login with admin credentials
const rootUser = _getEnv(MONGO_INITDB_ROOT_USERNAME);
const rootPassword = _getEnv(MONGO_INITDB_ROOT_PASSWORD);
if (rootUser !== "" && rootPassword !== "") {
    db.getSiblingDB(MONGO_ADMIN_DATABASE).auth(rootUser, rootPassword);
}

const username = _getEnv(MONGO_USERNAME);
const password = _getEnv(MONGO_PASSWORD);

const databases = [
    _getEnv(MONGO_INITDB_DATABASE),
    _getEnv(MONGO_ADDITIONAL_DATABASES)
].join(DATABASE_SEPARATOR).split(DATABASE_SEPARATOR);

var userRoles = [];
for (const databaseName of databases) {
    if (databaseName !== "") {
        userRoles.push({
            role: DATABASE_PERMISSIONS,
            db: databaseName
        });
    }
}

if (username !== "" && password !== "") {
    try {
        db.createUser(
            {
                user: username,
                pwd: password,
                roles: userRoles
            }
        );
    }
    catch(error) {
        // update the permissions for an existing user
        db.grantRolesToUser(username, userRoles);
    }
}
