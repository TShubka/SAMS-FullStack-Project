import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

/** Convenience accessor that also fails loudly if the provider is missing. */
export default function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside an <AuthProvider>');
  }
  return context;
}
