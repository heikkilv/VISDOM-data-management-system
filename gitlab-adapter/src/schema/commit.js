const mongoose = require('mongoose');
const { Schema } = mongoose;

const StatsSchema = new Schema({
  additions: Number,
  deletions: Number,
  total: Number,
});

const MetadataSchema = new Schema({
  last_modified: Date,
  api_version: Number,
  include_statistics: Boolean,
  include_link_files: Boolean,
  include_link_refs: Boolean,
  use_anonymization: Boolean,
});

const LinksSchema = new Schema({
  files: [{
    old_path: String,
    new_path: String,
    a_mode: String,
    b_mode: String,
    new_file: Boolean,
    renamed_file: Boolean,
    deleted_file: Boolean,
  }],
  refs: [{
    type: String,
    name: String,
  }],
});

const CommitSchema = new Schema({
  id: String,
  short_id: String,
  created_at: Date,
  parent_ids: [String],
  title: String,
  message: String,
  author_name: String,
  author_email: String,
  committer_name: String,
  committer_email: String,
  committed_date: Date,
  web_url: String,
  stats: StatsSchema,
  project_name: String,
  host_name: String,
  _metadata: MetadataSchema,
  _links: LinksSchema,
}, { collection : 'commits' });

const Commit = mongoose.model("Commit", CommitSchema);
module.exports = Commit;