import { TimeDeposit } from '../TimeDeposit'
import { TimeDepositCalculator } from '../TimeDepositCalculator'

describe('TimeDepositCalculator.updateBalance (characterization)', () => {

  test('basic plan adds interest after 30 days', () => {
    const calc = new TimeDepositCalculator()

    const plans = [new TimeDeposit(1, 'basic', 1000, 45)]

    calc.updateBalance(plans)

    expect(plans[0].balance).toBe(1000.83)
  })

})

describe('TimeDepositCalculator - parameterized', () => {

  test.each([
    ['basic', 1200, 45, 1201],
    ['student', 1200, 100, 1203],
    ['premium', 1200, 50, 1205]
  ])(
    'should update %s plan correctly',
    (planType, balance, days, expected) => {

      const calc = new TimeDepositCalculator()

      const plans = [new TimeDeposit(1, planType, balance, days)]

      calc.updateBalance(plans)

      expect(plans[0].balance).toBe(expected)
    }
  )

})

describe('TimeDepositCalculator - edge cases', () => {

  test('no interest before 30 days', () => {
    const calc = new TimeDepositCalculator()

    const plans = [new TimeDeposit(1, 'basic', 1000, 30)]

    calc.updateBalance(plans)

    expect(plans[0].balance).toBe(1000)
  })

  test('premium plan starts after 45 days only', () => {
    const calc = new TimeDepositCalculator()

    const plans = [new TimeDeposit(1, 'premium', 1000, 45)]

    calc.updateBalance(plans)

    expect(plans[0].balance).toBe(1000)
  })

  test('student plan stops after 365 days', () => {
    const calc = new TimeDepositCalculator()

    const plans = [new TimeDeposit(1, 'student', 1000, 366)]

    calc.updateBalance(plans)

    expect(plans[0].balance).toBe(1000)
  })

  test('empty array should not throw', () => {
    const calc = new TimeDepositCalculator()

    const plans: TimeDeposit[] = []

    expect(() => calc.updateBalance(plans)).not.toThrow()
  })

  test('multiple deposits handled correctly', () => {
    const calc = new TimeDepositCalculator()

    const plans = [
      new TimeDeposit(1, 'basic', 1200, 60),
      new TimeDeposit(2, 'student', 500, 200),
      new TimeDeposit(3, 'premium', 3000, 100),
    ]

    calc.updateBalance(plans)

    expect(plans[0].balance).toBe(1201)
    expect(plans[1].balance).toBe(501.25)
    expect(plans[2].balance).toBe(3012.5)
  })

})