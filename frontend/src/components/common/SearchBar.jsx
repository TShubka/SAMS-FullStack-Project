import { useEffect, useState } from 'react';

/**
 * Debounced search input. The debounce stops a request firing on every keystroke;
 * onSearch is called only after the user pauses.
 */
export default function SearchBar({ onSearch, placeholder = 'Search...', delay = 400 }) {
  const [value, setValue] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => onSearch(value.trim()), delay);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value]);

  return (
    <input
      className="search-bar"
      type="search"
      value={value}
      placeholder={placeholder}
      onChange={(e) => setValue(e.target.value)}
    />
  );
}
