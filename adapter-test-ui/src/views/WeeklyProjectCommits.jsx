import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, Label } from 'recharts';
import { useState, useEffect } from "react"
import { allAuthors, allProjects, weeklyProjectCommits } from '../queries';
import randomColor from 'node-random-color';

var colors = [];

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

  if (projects.length > 0) {
    for(let i = 0; i < projects.length; i++) {
      const color = randomColor({
        difference: 150,
        considerations: 5
      });
      colors.push(color);
    }
  }

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
        height={600}
        data={visualData}
        margin={{
            top: 5,
            right: 30,
            left: 20,
            bottom: 15,
        }}
        >
        <XAxis dataKey="week">
          <Label value="Week" offset={0} position="bottom" />
        </XAxis>
        <YAxis label={{ value: 'Number of commits', angle: -90, position: 'insideLeft', textAnchor: 'middle' }} />
        <Tooltip />
        <Legend verticalAlign="top" height={150}/>
        {authors.map((author, index) => (
            <Bar type="monotone" stackId="a" dataKey={`author_${author}`} fill={colors[index]} key={author}/>
        ))}
      </BarChart>
      <div className="selector">
        Select project:
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