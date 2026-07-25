import { useCallback, useEffect, useState } from 'react';
import { extractErrorMessage } from '../services/api';

/**
 * Runs an async fetcher and tracks the three states every screen needs:
 * loading, error and data. `deps` re-runs it (e.g. when a filter changes), and
 * `reload` re-runs it on demand after a create/update/delete.
 */
export default function useFetch(fetcher, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const run = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setData(await fetcher());
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    run();
  }, [run]);

  return { data, loading, error, reload: run, setData };
}
