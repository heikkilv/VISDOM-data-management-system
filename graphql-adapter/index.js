require('dotenv').config();

const express = require("express");
const app = express();
const connectDb = require("./src/connection");
const Commit = require("./src/schema/commit");

const PORT = process.env.HOST_PORT || 8080;

app.get("/", function (req, res) {
  res.send("hey")
})

app.get("/commits", async function (req, res) {
  const commits = await Commit.find({});
  res.json(commits);
})

app.listen(PORT, function() {
  console.log(`Listening on ${PORT}`);
  connectDb().then(() => {
    console.log("MongoDb connected");
  }).catch((err) => {
    console.log(err)
  });
});