require('dotenv').config();
const connectDb = require("./src/connection");
// const startAdapter = require("./src/graphql_server");
const express = require('express');
var cors = require('cors')
const gitlabRouter = require('./src/controllers/gitlab');

const app = express();
app.use(cors())

const port = process.env.HOST_PORT || 4000;

connectDb().then(() => {
  console.log("MongoDb connected");
}).catch((err) => {
  console.log(err)
});

// (async () => {
//   try {
//     await startAdapter()
//   } catch (e) {
//     console.log(e)
//     console.log("Server error")
//   }
// })();

app.use("/", gitlabRouter);

app.listen(port, () => {
  console.log(`Server listening at ${port}`)
});