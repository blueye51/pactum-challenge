import { useState } from 'react'

interface NumberInputProps {
  value: number
  onChange: (value: number) => void
  className?: string
  min?: number
  max?: number
  placeholder?: string
  wholeNumber?: boolean
}

export default function NumberInput({ value, onChange, className, min, max, placeholder, wholeNumber }: NumberInputProps) {
  const [text, setText] = useState(String(wholeNumber ? Math.round(value) : value))
  const [focused, setFocused] = useState(false)

  function parse(raw: string): number {
    const n = wholeNumber ? parseInt(raw, 10) : parseFloat(raw)
    return n
  }

  function handleChange(raw: string) {
    setText(raw)
    const n = parse(raw)
    if (!isNaN(n)) {
      onChange(n)
    }
  }

  function handleBlur() {
    setFocused(false)
    const n = parse(text)
    if (isNaN(n) || text.trim() === '') {
      const fallback = min ?? 0
      setText(String(fallback))
      onChange(fallback)
    } else {
      const clamped = wholeNumber ? Math.round(n) : n
      setText(String(clamped))
      onChange(clamped)
    }
  }

  function handleFocus() {
    setFocused(true)
    setText(String(value))
  }

  return (
    <input
      type="text"
      inputMode={wholeNumber ? "numeric" : "decimal"}
      className={className}
      value={focused ? text : String(value)}
      onChange={(e) => handleChange(e.target.value)}
      onFocus={handleFocus}
      onBlur={handleBlur}
      min={min}
      max={max}
      placeholder={placeholder}
    />
  )
}
