import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend } from 'recharts';
import { useState, useEffect } from "react"
import { allAuthors, allProjects, weeklyProjectCommits } from '../queries';

const colors = ["#8884d8", "#82ca9d", "#ff776e"];

export default function WeeklyProjectCommits({startDate, endDate}) {
  const [selectedProject, setSelectedProject] = useState("");
  const [authors, setAuthors] = useState([]);
  const [projects, setProjects] = useState([]);
  const [visualData, setVisualData] = useState([]);
  
  useEffect(() => {
    async function prefetch() {
      const authorsRes = await allAuthors()
      const projectsRes = await allProjects()
      setAuthors(authorsRes.data)
      setProjects(projectsRes.data)
      if (projectsRes.data.length > 0) {
        setSelectedProject(projectsRes.data[0])
        const result = await weeklyProjectCommits(projectsRes.data[0], startDate.getTime(), endDate.getTime())
        setVisualData(result.data)
      }
    }
    prefetch()
  }, [startDate, endDate])

  const onChangeValue = async (event) => {
    setSelectedProject(event.target.value)
    const result = await weeklyProjectCommits(event.target.value, startDate.getTime(), endDate.getTime())
    setVisualData(result.data)
  }

  if (!(authors.length > 0) || !(projects.length > 0) || !(visualData.length > 0))  {
    return <div>loading...</div>
  }

  return (
    <div className="visualisation">
      <BarChart
        width={800}
        height={800}
        data={visualData}
        margin={{
            top: 5,
            right: 30,
            left: 20,
            bottom: 5,
        }}
        >
        <XAxis dataKey="week" />
        <YAxis />
        <Tooltip />
        <Legend />
        {authors.map((author, index) => (
            <Bar type="monotone" stackId="a" dataKey={`author_${author}`} fill={colors[index]} key={author}/>
        ))}
      </BarChart>
      <div className="selector">
        {projects.map(project => (
          <div>
            <input key={project} type="radio" value={project} id={project} checked={selectedProject === project} onChange={onChangeValue}/>
            <label for={project}>{project}</label>
          </div>
        ))}
      </div>
    </div>
  );
}