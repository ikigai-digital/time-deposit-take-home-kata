import { TimeDeposit } from '../TimeDeposit'

export interface InterestStrategy {
  calculate(deposit: TimeDeposit): number
}


