import { useState } from 'react'

interface HelpTipProps {
  text: string
}

export default function HelpTip({ text }: HelpTipProps) {
  const [open, setOpen] = useState(false)

  return (
    <span className="help-tip">
      <button
        type="button"
        className="help-btn"
        onClick={(e) => { e.preventDefault(); setOpen(!open) }}
      >
        ?
      </button>
      {open && <span className="help-text">{text}</span>}
    </span>
  )
}
