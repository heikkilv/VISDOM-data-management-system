require('dotenv').config();
const connectDb = require("./src/connection");
const startAdapter = require("./src/graphql_server");

connectDb().then(() => {
  console.log("MongoDb connected");
}).catch((err) => {
  console.log(err)
});

(async () => {
  try {
    await startAdapter()
  } catch (e) {
    console.log(e)
    console.log("Server error")
  }
})();