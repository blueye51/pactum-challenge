import { useState } from 'react'

interface NumberInputProps {
  value: number
  onChange: (value: number) => void
  className?: string
  min?: number
  max?: number
  placeholder?: string
}

export default function NumberInput({ value, onChange, className, min, max, placeholder }: NumberInputProps) {
  const [text, setText] = useState(String(value))
  const [focused, setFocused] = useState(false)

  function handleChange(raw: string) {
    setText(raw)
    const n = parseFloat(raw)
    if (!isNaN(n)) {
      onChange(n)
    }
  }

  function handleBlur() {
    setFocused(false)
    const n = parseFloat(text)
    if (isNaN(n) || text.trim() === '') {
      const fallback = min ?? 0
      setText(String(fallback))
      onChange(fallback)
    } else {
      setText(String(n))
      onChange(n)
    }
  }

  function handleFocus() {
    setFocused(true)
    setText(String(value))
  }

  return (
    <input
      type="text"
      inputMode="decimal"
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
