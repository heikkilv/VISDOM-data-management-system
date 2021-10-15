const Commit = require("./schema/commit");

const divideTimeRangeIntoWeeks = (startDate, endDate) => {
  const startDateWeek = new Date(startDate).getTime() / 1000 / 60 / 60 / 24 / 7;
  const endDateWeek = new Date(endDate).getTime() / 1000 / 60 / 60 / 24 / 7;
  const weeks = [];
  for (let i = startDateWeek; i <= endDateWeek; i++) {
    const week = {
      startDate: new Date(startDate).setTime(i * 7 * 24 * 60 * 60 * 1000),
      endDate: new Date(startDate).setTime((i + 1) * 7 * 24 * 60 * 60 * 1000),
    };
    weeks.push({
      startDate: new Date(week.startDate).toISOString(),
      endDate: new Date(week.endDate).toISOString(),
    });
  }
  return weeks;
};

const buildWeeklyProjectCommitsNums = async (projectName, startDate, endDate) => {
  const result = []
  const projectCommits = await Commit.find({project_name: projectName})
  const allAuthorNames = await Commit.distinct('author_name');
  const weeks = divideTimeRangeIntoWeeks(startDate, endDate);
  let weekIndex = 1;
  for (const week of weeks) {
    let weekObject = {
      week: weekIndex,
    }
    const weeklyCommits = projectCommits.filter(commit => {
      return new Date(commit.created_at).getTime() >= new Date(week.startDate).getTime() && new Date(commit.created_at).getTime() <= new Date(week.endDate).getTime();
    });
    for (const authorName of allAuthorNames) {
      const weeklyCommitsByAuthor = weeklyCommits.filter(commit => {
        return commit.author_name === authorName;
      });
      weekObject[`author_${authorName}`] = weeklyCommitsByAuthor.length ? weeklyCommitsByAuthor.length : 0;
      weekIndex++;
    }
    result.push(weekObject);
  }
  return result
}

const buildWeeklyPersonCommitsNums = async (authorName, startDate, endDate) => {
  const result = []
  const authorCommits = await Commit.find({author_name: authorName})
  const allProjectNames = await Commit.distinct('project_name');
  const weeks = divideTimeRangeIntoWeeks(startDate, endDate);
  let weekIndex = 1;
  for (const week of weeks) {
    let weekObject = {
      week: weekIndex,
    }
    const weeklyCommits = authorCommits.filter(commit => {
      return new Date(commit.created_at).getTime() >= new Date(week.startDate).getTime() && new Date(commit.created_at).getTime() <= new Date(week.endDate).getTime();
    });
    for (const projectName of allProjectNames) {
      const weeklyCommitsByProject = weeklyCommits.filter(commit => {
        return commit.project_name === projectName && commit.author_name === authorName;
      });
      weekObject[`project_${projectName}`] = weeklyCommitsByProject.length ? weeklyCommitsByProject.length : 0;
      weekIndex++;
    }
    result.push(weekObject);
  }
  return result
}

module.exports = {
  buildWeeklyProjectCommitsNums,
  buildWeeklyPersonCommitsNums,
}