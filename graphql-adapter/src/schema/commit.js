const mongoose = require('mongoose');
const { Schema } = mongoose;

const commitSchema = new Schema({
  project_name: String,
  committer_name: String,
  committed_date: String
}, { collection : 'commits' });

const Commit = mongoose.model("Commit", commitSchema);
module.exports = Commit;