import { useEffect, useRef, useCallback } from 'react'

interface Options {
  onScan: (barcode: string) => void
  minLength?: number
  maxGapMs?: number
  enabled?: boolean
}

/**
 * Escuta globalmente as teclas do leitor de código de barras USB-HID.
 * Leitores emitem os dígitos muito rápido e terminam com Enter.
 * Se o gap entre teclas exceder maxGapMs, o buffer é descartado
 * (digitação manual do operador, não leitura de código).
 */
export function useBarcodeScan({
  onScan,
  minLength = 8,
  maxGapMs = 80,
  enabled = true,
}: Options) {
  const buffer  = useRef('')
  const timer   = useRef<ReturnType<typeof setTimeout>>()

  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (!enabled) return

      if (e.key === 'Enter') {
        const code = buffer.current.trim()
        if (code.length >= minLength) onScan(code)
        buffer.current = ''
        clearTimeout(timer.current)
        return
      }

      if (e.key.length === 1 && /[\d]/.test(e.key)) {
        buffer.current += e.key
        clearTimeout(timer.current)
        timer.current = setTimeout(() => {
          buffer.current = ''
        }, maxGapMs)
      }
    },
    [onScan, minLength, maxGapMs, enabled]
  )

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown)
    return () => {
      window.removeEventListener('keydown', handleKeyDown)
      clearTimeout(timer.current)
    }
  }, [handleKeyDown])
}
