import { useState } from 'react';
import './App.css';
import WeeklyProjectCommits from "./views/WeeklyProjectCommits";
import WeeklyPersonCommits from "./views/WeeklyPersonCommits";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

function App() {
  const [startDate, setStartDate] = useState(new Date(1620897656000));
  const [endDate, setEndDate] = useState(new Date(1631527527000));
  const [visualisation, setVisualisation] = useState(0)
  return (
    <div className="App">
      <div className="buttonRow">
        <button className={visualisation ? "visualisationButton" : "visualisationButtonActive"} onClick={() => setVisualisation(0)}>Weekly commits made by an author to projects</button>
        <button className={visualisation ? "visualisationButtonActive" : "visualisationButton"} onClick={() => setVisualisation(1)}>Weekly commits made by authors to one project</button>
      </div>
      <div className="timeRow">
        <DatePicker selected={startDate} onChange={(date) => setStartDate(date)} />
        <span className="separator">-</span>
        <DatePicker selected={endDate} onChange={(date) => setEndDate(date)} />
      </div>
      {visualisation 
        ? <WeeklyProjectCommits startDate={startDate} endDate={endDate}/> 
        : <WeeklyPersonCommits startDate={startDate} endDate={endDate}/>
      }
    </div>
  );
}

export default App;
