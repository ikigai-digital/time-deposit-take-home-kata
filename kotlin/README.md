# Time Deposit Refactoring Kata - Take home assignment
## XA bank time deposit

A junior developer started working on a jira story but was only able to implement the domain logic before leaving the company.

We were able to capture some of the business requirements, which are listed below. If there is any logic implemented that is not listed in the requirements, please consider the implemented code as the source of truth.

- An API endpoint should be provided for the frontend to call, allowing the balance of all accounts in the database to be updated.
    - This functionality is not currently implemented in the codebase. Please feel free to use any API framework of your choice to accomplish this.
- All time deposit plans must be stored in the Postgres database.
    - Please ensure that your solution includes the ability to store these tables and their rows in a database (for example, Docker, SQLite, etc.).
    - timeDeposits
        - id: Integer (primary key)
        - planType: String (required)
        - days: Integer (required)
        - balance: Decimal (required)
    - withdrawals
        - id: Integer (primary key)
        - timeDepositId: integer (Foreign Key) (required)
        - amount: Decimal (required)
        - date: Date (required)
- The system should calculate the monthly interest based on the type of plan.
- No interest will be added to most time deposit plans for the first 30 days.
- There are currently 3 types of time deposit plans available at the bank: basic, student, and premium.
    - basic plan: 1%
    - student plan: 3%
    - premium plan: 5%
- The student plan will not accumulate further interest after 1 year.
- The premium plan will not begin accumulating interest until after 45 days.

We would like you to refactor the solution to ensure that the implementation meets all the requirements outlined above.
You can assume that the domain logic has already been tested and is functioning correctly, with no bugs reported.

Limitations:
- The TimeDeposit class is shared across multiple classes; therefore, you must ensure that no breaking changes are introduced to the TimeDeposit class.
- The updateBalance method is used by other classes; therefore, you must not modify its method signature.

Important notes:
- Please ensure that no existing functionalities are broken during the refactoring process.
- The TimeDeposit class contains all the necessary information to evaluate the applicable interest rate. If additional criteria are to be considered, we expect them to be added to the TimeDeposit class.
- The criteria in evaluating the interest rate to become extremely complicated as the product grow, so please be sure your design is ready to extend.
- Aside from the limitations listed above, feel free to update/change anything as you see fit such as framework version, library version, etc
- We at ikigai value SOLID, Design patterns, Clean/Hexagonal Architecture, and common clean code practices. Be ready to justify the approach that you take.
- Please include instructions on how to run the application upon submission.