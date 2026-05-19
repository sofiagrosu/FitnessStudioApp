import { CalendarDays, Clock, MapPin, User } from "lucide-react";

function CourseCard({ course }) {
  const occupancyPercent = Math.round(
    (course.currentOccupancy / course.maxCapacity) * 100
  );

  return (
    <div className="course-card card">
      <div className="course-image">
        <img src={course.image} alt={course.name} />
      </div>

      <div className="course-content">
        <div className="course-top">
          <div>
            <h3>{course.name}</h3>
            <p>{course.type}</p>
          </div>
          <span className="tag">{course.level}</span>
        </div>

        <div className="course-info">
          <span><User size={15} /> {course.trainer}</span>
          <span><CalendarDays size={15} /> {course.days}</span>
          <span><Clock size={15} /> {course.time}</span>
          <span><MapPin size={15} /> {course.location}</span>
        </div>

        <div className="occupancy">
          <div className="occupancy-row">
            <span>{course.currentOccupancy}/{course.maxCapacity}</span>
            <span>{occupancyPercent}% full</span>
          </div>
          <div className="progress">
            <div style={{ width: `${occupancyPercent}%` }} />
          </div>
        </div>

        <button className="primary-btn course-btn">
          {occupancyPercent >= 100 ? "Join waitlist" : "Join class"}
        </button>
      </div>
    </div>
  );
}

export default CourseCard;