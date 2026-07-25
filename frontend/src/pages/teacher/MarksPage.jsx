import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import courseService from '../../services/courseService';
import enrollmentService from '../../services/enrollmentService';
import gradeService from '../../services/gradeService';
import useAuth from '../../hooks/useAuth';
import useFetch from '../../hooks/useFetch';
import { extractErrorMessage } from '../../services/api';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';
import EmptyState from '../../components/common/EmptyState';
import Modal from '../../components/common/Modal';
import { ASSESSMENT_TYPES } from '../../utils/constants';

/**
 * Teacher (and admin) marks entry. Owner: Member 3.
 *
 * Pick a course, then enter one assessment's marks for the whole roster. The grid
 * shows each student's existing mark so the teacher edits in place; the backend
 * enforces the mark <= maxMarks rule and returns a readable 400 that surfaces here.
 */
export default function MarksPage() {
  const { isAdmin } = useAuth();
  const [params] = useSearchParams();
  const [courseId, setCourseId] = useState(params.get('courseId') || '');
  const [assessmentId, setAssessmentId] = useState('');
  const [assessments, setAssessments] = useState([]);
  const [roster, setRoster] = useState([]); // [{ enrollmentId, name, roll, markId, value, max }]
  const [loading, setLoading] = useState(false);
  const [banner, setBanner] = useState(null);
  const [showAssessmentForm, setShowAssessmentForm] = useState(false);

  const { data: courses } = useFetch(
    () => (isAdmin ? courseService.list({ size: 200 }) : courseService.myCourses()),
    [isAdmin],
  );
  const courseList = isAdmin ? (courses?.content || []) : (courses || []);

  const loadAssessments = async (cid) => {
    if (!cid) { setAssessments([]); setAssessmentId(''); return; }
    const list = await gradeService.assessmentsByCourse(cid);
    setAssessments(list);
    if (list.length > 0 && !list.find((a) => String(a.id) === assessmentId)) {
      setAssessmentId(String(list[0].id));
    } else if (list.length === 0) {
      setAssessmentId('');
    }
  };

  useEffect(() => {
    setBanner(null);
    loadAssessments(courseId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [courseId]);

  // Build the mark grid from the roster and the marks already recorded.
  useEffect(() => {
    if (!courseId || !assessmentId) { setRoster([]); return; }
    let cancelled = false;
    setLoading(true);
    const assessment = assessments.find((a) => String(a.id) === String(assessmentId));
    Promise.all([
      enrollmentService.byCourse(courseId),
      gradeService.marksByCourse(courseId),
    ])
      .then(([enrollments, marks]) => {
        if (cancelled) return;
        const forThis = marks.filter((m) => String(m.assessmentId) === String(assessmentId));
        setRoster(enrollments.map((e) => {
          const existing = forThis.find((m) => m.enrollmentId === e.id);
          return {
            enrollmentId: e.id,
            name: e.studentName,
            roll: e.rollNumber,
            markId: existing?.id ?? null,
            value: existing ? String(existing.marksObtained) : '',
            max: assessment?.maxMarks,
          };
        }));
      })
      .catch((err) => !cancelled && setBanner({ type: 'error', text: extractErrorMessage(err) }))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [courseId, assessmentId, assessments]);

  const setValue = (enrollmentId, value) =>
    setRoster((r) => r.map((row) => (row.enrollmentId === enrollmentId ? { ...row, value } : row)));

  const saveRow = async (row) => {
    if (row.value === '') return;
    setBanner(null);
    try {
      if (row.markId) {
        await gradeService.updateMark(row.markId, {
          enrollmentId: row.enrollmentId, assessmentId: Number(assessmentId),
          marksObtained: Number(row.value),
        });
      } else {
        const created = await gradeService.createMark({
          enrollmentId: row.enrollmentId, assessmentId: Number(assessmentId),
          marksObtained: Number(row.value),
        });
        setRoster((r) => r.map((x) => (x.enrollmentId === row.enrollmentId ? { ...x, markId: created.id } : x)));
      }
      setBanner({ type: 'success', text: `Saved ${row.roll}` });
    } catch (err) {
      setBanner({ type: 'error', text: extractErrorMessage(err) });
    }
  };

  const currentAssessment = assessments.find((a) => String(a.id) === String(assessmentId));

  return (
    <section>
      <h1>Enter Marks</h1>

      {banner && <Alert type={banner.type} onDismiss={() => setBanner(null)}>{banner.text}</Alert>}

      <div className="card">
        <div className="filter-row">
          <select value={courseId} onChange={(e) => setCourseId(e.target.value)}>
            <option value="">Select a course...</option>
            {courseList.map((c) => <option key={c.id} value={c.id}>{c.code} — {c.title}</option>)}
          </select>
          {courseId && (
            <select value={assessmentId} onChange={(e) => setAssessmentId(e.target.value)}>
              <option value="">Select assessment...</option>
              {assessments.map((a) => (
                <option key={a.id} value={a.id}>{a.title} (max {a.maxMarks}, {a.weightPercent}%)</option>
              ))}
            </select>
          )}
          {courseId && (
            <button className="btn btn-secondary inline" onClick={() => setShowAssessmentForm(true)}>
              + Assessment
            </button>
          )}
        </div>
      </div>

      {!courseId && <EmptyState title="Select a course to begin" />}
      {courseId && assessments.length === 0 && (
        <EmptyState title="No assessments yet"
          message="Add an assessment (quiz, midterm, final...) before entering marks." />
      )}

      {courseId && assessmentId && loading && <Spinner label="Loading marks..." />}

      {courseId && assessmentId && !loading && roster.length > 0 && (
        <div className="card">
          <p className="muted">
            {currentAssessment?.title} · max {currentAssessment?.maxMarks} · weight {currentAssessment?.weightPercent}%
          </p>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Roll</th><th>Student</th><th>Marks</th><th></th></tr></thead>
              <tbody>
                {roster.map((row) => (
                  <tr key={row.enrollmentId}>
                    <td>{row.roll}</td>
                    <td>{row.name}</td>
                    <td>
                      <input type="number" className="mark-input" value={row.value}
                        min="0" max={row.max} step="0.01"
                        onChange={(e) => setValue(row.enrollmentId, e.target.value)}
                        style={{ width: '90px', marginTop: 0 }} />
                      <span className="muted"> / {row.max}</span>
                    </td>
                    <td>
                      <button className="btn-link" onClick={() => saveRow(row)}>
                        {row.markId ? 'Update' : 'Save'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showAssessmentForm && (
        <AssessmentForm courseId={courseId}
          onClose={() => setShowAssessmentForm(false)}
          onSaved={async (text) => {
            setShowAssessmentForm(false);
            setBanner({ type: 'success', text });
            await loadAssessments(courseId);
          }} />
      )}
    </section>
  );
}

function AssessmentForm({ courseId, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: '', type: 'QUIZ', maxMarks: 10, weightPercent: 10,
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [saving, setSaving] = useState(false);

  const change = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: undefined });
  };

  const validate = () => {
    const n = {};
    if (!form.title.trim()) n.title = 'Title is required';
    if (Number(form.maxMarks) <= 0) n.maxMarks = 'Must be greater than zero';
    if (Number(form.weightPercent) < 0 || Number(form.weightPercent) > 100) n.weightPercent = '0 to 100';
    setErrors(n);
    return Object.keys(n).length === 0;
  };

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;
    setSaving(true);
    try {
      await gradeService.createAssessment({
        courseId: Number(courseId), title: form.title, type: form.type,
        maxMarks: Number(form.maxMarks), weightPercent: Number(form.weightPercent),
      });
      onSaved(`Assessment "${form.title}" added`);
    } catch (err) {
      setServerError(extractErrorMessage(err));
    } finally { setSaving(false); }
  };

  return (
    <Modal title="New assessment" onClose={onClose}>
      <form onSubmit={submit} noValidate>
        <Alert type="error" onDismiss={() => setServerError('')}>{serverError}</Alert>

        <label htmlFor="title">Title</label>
        <input id="title" name="title" value={form.title} onChange={change} disabled={saving} />
        {errors.title && <span className="field-error">{errors.title}</span>}

        <label htmlFor="type">Type</label>
        <select id="type" name="type" value={form.type} onChange={change} disabled={saving}>
          {ASSESSMENT_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
        </select>

        <div className="form-row">
          <div>
            <label htmlFor="maxMarks">Maximum marks</label>
            <input id="maxMarks" name="maxMarks" type="number" value={form.maxMarks} onChange={change} disabled={saving} />
            {errors.maxMarks && <span className="field-error">{errors.maxMarks}</span>}
          </div>
          <div>
            <label htmlFor="weightPercent">Weight %</label>
            <input id="weightPercent" name="weightPercent" type="number" value={form.weightPercent} onChange={change} disabled={saving} />
            {errors.weightPercent && <span className="field-error">{errors.weightPercent}</span>}
          </div>
        </div>

        <div className="modal-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={saving}>Cancel</button>
          <button type="submit" className="btn btn-primary inline" disabled={saving}>
            {saving ? 'Saving...' : 'Add'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
