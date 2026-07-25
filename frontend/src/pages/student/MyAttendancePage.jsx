import attendanceService from '../../services/attendanceService';
import studentService from '../../services/studentService';
import useFetch from '../../hooks/useFetch';
import DataTable from '../../components/common/DataTable';

/** A student's own attendance records. Owner: Member 3. */
export default function MyAttendancePage() {
  const { data: me } = useFetch(() => studentService.me(), []);
  const { data, loading, error } = useFetch(
    () => (me ? attendanceService.byStudent(me.id) : Promise.resolve([])),
    [me?.id],
  );

  return (
    <section>
      <h1>My Attendance</h1>
      <DataTable
        loading={loading || !me}
        error={error}
        rows={data}
        emptyTitle="No attendance recorded yet"
        columns={[
          { key: 'attendanceDate', label: 'Date' },
          { key: 'courseCode', label: 'Course' },
          { key: 'courseTitle', label: 'Title' },
          { key: 'status', label: 'Status', render: (r) => <StatusPill status={r.status} /> },
          { key: 'remarks', label: 'Remarks', render: (r) => r.remarks || '—' },
        ]}
      />
    </section>
  );
}

function StatusPill({ status }) {
  const cls = status === 'PRESENT' ? 'grade-top' : status === 'LATE' ? 'grade-mid' : 'grade-fail';
  return <span className={`grade-badge ${cls}`}>{status}</span>;
}
