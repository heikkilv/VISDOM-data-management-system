const { ApolloServer, gql } = require('apollo-server')
const { GraphQLScalarType, Kind } = require('graphql');
const Commit = require("./schema/commit");
const {buildWeeklyProjectCommitsNums, buildWeeklyPersonCommitsNums} = require("./processData")

const getPrerequisiteData = async () => {
  const allAuthorNames = await Commit.distinct('author_name')
  const allProjectNames = await Commit.distinct('project_name')
  const authorNamesQuery = allAuthorNames.map(name => `author_${name}: Int!`).join("\n")
  const projectNamesQuery = allProjectNames.map(name => `project_${name}: Int!`).join("\n")
  return {authorNamesQuery, projectNamesQuery}
}

const startAdapter = async () => {
  const {authorNamesQuery, projectNamesQuery} = await getPrerequisiteData()
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
      committer_name: String
      committer_email: String
      committed_date: Date
      web_url: String
      stats: Stats
      project_name: String
      host_name: String
      _metadata: Metadata
      _links: Links
    }

    type ProjectCommitsNum {
      authorName: String!
      commitsNum: Int!
    }

    type PersonCommitsNum {
      projectName: String!
      commitsNum: Int!
    }

    type WeeklyProjectCommitsNum {
      week: Int!,
      ${authorNamesQuery}
    }

    type WeeklyPersonCommitsNum {
      week: Int!,
      ${projectNamesQuery}
    }

    type Query {
      commitsCount: Int!
      allCommits: [Commit!]!
      allAuthorNames: [String!]!
      allProjectNames: [String!]!
      commitById(id: String!): Commit
      commitByCreatedDate(createdAt: Date!): Commit
      weeklyProjectCommitsNums(projectName: String!, startDate: Date!, endDate: Date!): [WeeklyProjectCommitsNum!]!
      weeklyPersonCommitsNums(authorName: String!, startDate: Date!, endDate: Date!): [WeeklyPersonCommitsNum!]!
    }
  `

  const resolvers = {
    Date: dateScalar,
    Query: {
      commitsCount: () => Commit.collection.countDocuments(),
      allCommits: () => {
        return Commit.find({})
      },
      allProjectNames : () => Commit.distinct('project_name'),
      allAuthorNames : () => Commit.distinct('author_name'),
      commitById: (_, args) => Commit.findById(args.id),
      commitByCreatedDate: (_, args) => Commit.findOne({createdAt: args.createdAt}),
      weeklyProjectCommitsNums: (_, args) => buildWeeklyProjectCommitsNums(args.projectName, args.startDate, args.endDate),
      weeklyPersonCommitsNums: (_, args) => buildWeeklyPersonCommitsNums(args.authorName, args.startDate, args.endDate),
    }
  }

  const server = new ApolloServer({
    typeDefs,
    resolvers,
  })

  server.listen().then(({ url }) => {
    console.log(`Server ready at ${url}`)
  })
}

module.exports = startAdapter