require('dotenv').config();
const { ApolloServer, gql } = require('apollo-server')
const { GraphQLScalarType, Kind } = require('graphql');
const connectDb = require("./src/connection");
const Commit = require("./src/schema/commit");


connectDb().then(() => {
  console.log("MongoDb connected");
}).catch((err) => {
  console.log(err)
});

const typeDefs = gql`
  scalar Date

  type Stats {
    additions: Int
    deletions: Int
    total: Int
  }

  type Metadata {
    last_modified: Date
    api_version: Int
    include_statistics: Boolean
    include_link_files: Boolean
    include_link_refs: Boolean
    use_anonymization: Boolean
  }

  type File {
    old_path: String
    new_path: String
    a_mode: String
    b_mode: String
    new_file: Boolean
    renamed_file: Boolean
    deleted_file: Boolean
  }

  type Ref {
    type: String
    name: String
  }

  type Links {
    files: [File]
    refs: [Ref]
  }

  type Commit {
    id: String!
    short_id: String
    created_at: Date
    parent_ids: [String!]
    title: String
    message: String
    author_name: String
    author_email: String
    author_date: Date
    committer_name: String
    committer_email: String
    committer_date: Date
    web_url: String
    stats: Stats
    project_name: String
    host_name: String
    _metadata: Metadata
    _links: Links
  }

  type Query {
    commitsCount: Int!
    allCommits: [Commit!]!
    commitById(id: String!): Commit
  }
`

const dateScalar = new GraphQLScalarType({
  name: 'Date',
  description: 'Date custom scalar type',
  serialize(value) {
    return value.getTime(); // Convert outgoing Date to integer for JSON
  },
  parseValue(value) {
    return new Date(value); // Convert incoming integer to Date
  },
  parseLiteral(ast) {
    if (ast.kind === Kind.INT) {
      return new Date(parseInt(ast.value, 10)); // Convert hard-coded AST string to integer and then to Date
    }
    return null; // Invalid hard-coded value (not an integer)
  },
});

const resolvers = {
  Date: dateScalar,
  Query: {
    commitsCount: () => Commit.collection.countDocuments(),
    allCommits: (root, args) => {
      return Commit.find({})
    },
    commitById: (root, args) => Commit.findById(args.id)
  }
}

const server = new ApolloServer({
  typeDefs,
  resolvers,
})

server.listen().then(({ url }) => {
  console.log(`Server ready at ${url}`)
})