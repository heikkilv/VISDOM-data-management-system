import { useState, useEffect } from "react"
import { LineChart, Line, XAxis, YAxis, Tooltip, Legend } from 'recharts';
import { allAuthors, allProjects, weeklyPersonCommits } from '../queries';
import "./visualisation.css";

const colors = ["#8884d8", "#82ca9d", "#ff776e"];

export default function WeeklyPersonCommits({startDate, endDate}) {
  const [selectedPerson, setSelectedPerson] = useState("");
  const [authors, setAuthors] = useState([]);
  const [projects, setProjects] = useState([]);
  const [visualData, setVisualData] = useState([]);
  
  useEffect(() => {
    async function prefetch() {
      const authorsRes = await allAuthors()
      const projectsRes = await allProjects()
      setAuthors(authorsRes.data)
      setProjects(projectsRes.data)
      if (authorsRes.data.length > 0) {
        setSelectedPerson(authorsRes.data[0])
        const result = await weeklyPersonCommits(authorsRes.data[0], startDate.getTime(), endDate.getTime())
        setVisualData(result.data)
      }
    }
    prefetch()
  }, [startDate, endDate])

  const onChangeValue = async (event) => {
    setSelectedPerson(event.target.value)
    const result = await weeklyPersonCommits(event.target.value, startDate.getTime(), endDate.getTime())
    setVisualData(result.data)
  }

  if (!(authors.length > 0) || !(projects.length > 0) || !(visualData.length > 0))  {
    return <div>loading...</div>
  }

  return (
    <div className="visualisation">
      <LineChart
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
        {projects.map((project, index) => (
            <Line type="monotone" dataKey={`project_${project}`} stroke={colors[index]} key={project}/>
        ))}
      </LineChart>
      <div className="selector">
        {authors.map(author => (
          <div>
            <input key={author} type="radio" value={author} id={author} checked={selectedPerson === author} onChange={onChangeValue}/>
            <label for={author}>{author}</label>
          </div>
        ))}
      </div>
    </div>
  );
}