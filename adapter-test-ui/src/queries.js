import axios from 'axios';
const uri = `http://localhost:${process.env.REACT_APP_ADAPTER_PORT}`

export const allCommits = async () => {
  try {
    const response = await axios.get(`${uri}/commits`);
    return response;
  } catch (error) {
    console.error(error);
    throw error;
  }
}

export const allAuthors = async () => {
  try {
    const response = await axios.get(`${uri}/authors`);
    return response;
  } catch (error) {
    console.error(error);
    throw error;
  }
}

export const allProjects = async () => {
  try {
    const response = await axios.get(`${uri}/projects`);
    return response;
  } catch (error) {
    console.error(error);
    throw error;
  }
}

export const weeklyProjectCommits = async (projectName, startDate, endDate) => {
  try {
    const response = await axios.get(`${uri}/weekly-project-commits`, {
      params: {
        projectName,
        startDate,
        endDate
      }
    });
    return response;
  } catch (error) {
    console.error(error);
    throw error;
  }
}

export const weeklyPersonCommits = async (authorName, startDate, endDate) => {
  try {
    const response = await axios.get(`${uri}/weekly-person-commits`, {
      params: {
        authorName,
        startDate,
        endDate
      }
    });
    return response;
  } catch (error) {
    console.error(error);
    throw error;
  }
}