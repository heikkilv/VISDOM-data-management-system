const gitlabRouter = require("express").Router();
const Commit = require("../schema/commit");
const {buildWeeklyProjectCommitsNums, buildWeeklyPersonCommitsNums} = require("../processData")

gitlabRouter.get("/commits", async (req, res) => {
  const commits = await Commit.find({});
  res.status(200).json(commits);
})

gitlabRouter.get("/authors", async (req, res) => {
  const authors = await Commit.distinct('author_name');
  res.status(200).json(authors);
})

gitlabRouter.get("/projects", async (req, res) => {
  const projects = await Commit.distinct('project_name');
  res.status(200).json(projects);
})

gitlabRouter.get("/weekly-project-commits", async (req, res) => {
  const projectName = req.query.projectName;
  const startDate = Number(req.query.startDate);
  const endDate = Number(req.query.endDate);
  const weeklyProjectCommitsNums = await buildWeeklyProjectCommitsNums(projectName, startDate, endDate);
  res.status(200).json(weeklyProjectCommitsNums);
})

gitlabRouter.get("/weekly-person-commits", async (req, res) => {
  const authorName = req.query.authorName;
  const startDate = Number(req.query.startDate);
  const endDate = Number(req.query.endDate);
  const weeklyPersonCommitsNums = await buildWeeklyPersonCommitsNums(authorName, startDate, endDate);
  res.status(200).json(weeklyPersonCommitsNums);
})

module.exports = gitlabRouter;