const mongoose = require("mongoose");
const url = `mongodb://${process.env.MONGODB_HOST}:${process.env.MONGODB_PORT}/${process.env.MONGODB_DATABASE}`;
console.log(url)

const connectMongo = () => {
 return mongoose.connect(url, { 
    user: process.env.MONGODB_USERNAME,
    pass: process.env.MONGODB_PASSWORD,
    authSource: process.env.MONGODB_METADATA_DATABASE,
    useNewUrlParser: true,
    useUnifiedTopology: true
  });
};
module.exports = connectMongo;