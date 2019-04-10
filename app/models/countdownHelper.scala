package models

import java.time.Duration

import cats.data.NonEmptyList

object countdownHelper {

  trait DB {
    def standups: List[Standup]
  }

  object InMemoryDB extends DB {
    override def standups: List[Standup] = List(
      Standup(id = 1, name = "main", displayName="Access UK Main Standup", teams = NonEmptyList(
        Team(id = 1, name = "Releases", speaker = "Steff", Duration.ofSeconds(45)),
        List(
          Team(id = 2, name = "Fes", speaker = "Tommy", Duration.ofSeconds(45)),
          Team(id = 3, name = "L3", speaker = "David", Duration.ofSeconds(45)),
          Team(id = 4, name = "Out of Country", speaker = "Victor", Duration.ofSeconds(45)),
          Team(id = 5, name = "CI", speaker = "Katie", Duration.ofSeconds(45)),
          Team(id = 6, name = "Tech CI", speaker = "Dominic", Duration.ofSeconds(45)),
          Team(id = 7, name = "Standard Sections", speaker = "Iuliana", Duration.ofSeconds(45)),
          Team(id = 8, name = "Small projects", speaker = "Jeremy", Duration.ofSeconds(45)),
          Team(id = 9, name = "CWI", speaker = "Shiv", Duration.ofSeconds(45)),
          Team(id = 10, name = "Actions", speaker = "Matt", Duration.ofSeconds(45))
        ),
      )),
      Standup(id = 2, name = "ba", displayName="Access UK BA Standup",teams = NonEmptyList(
        Team(id = 1, name = "Katie", speaker = "Katie", Duration.ofSeconds(90)),
        List(
          Team(id = 2, name = "Kate", speaker = "Kate", Duration.ofSeconds(45)),
          Team(id = 3, name = "Samier", speaker = "Samier", Duration.ofSeconds(45)),
          Team(id = 4, name = "Jeremy", speaker = "Jeremy", Duration.ofSeconds(45)),
          Team(id = 5, name = "Thomas", speaker = "Thomas", Duration.ofSeconds(45)),
          Team(id = 6, name = "Tommy", speaker = "Tommy", Duration.ofSeconds(45)),
          Team(id = 7, name = "Harry", speaker = "Harry", Duration.ofSeconds(45)),
          Team(id = 8, name = "Alice", speaker = "Alice", Duration.ofSeconds(45)),
          Team(id = 9, name = "Dean", speaker = "Dean", Duration.ofSeconds(45)),
          Team(id = 10, name = "Eoin", speaker = "Eoin", Duration.ofSeconds(45)),
          Team(id = 11, name = "Victor", speaker = "Victor", Duration.ofSeconds(45)),
          Team(id = 12, name = "Fred", speaker = "Fred", Duration.ofSeconds(45)),
          Team(id = 13, name = "Jamie", speaker = "Jamie", Duration.ofSeconds(45))
        )
      )),
      Standup(id = 3, name = "team5", displayName="Access UK Team 5 Standup",teams = NonEmptyList(
        Team(id = 1, name = "Team 5", speaker = "Cristi", Duration.ofSeconds(90)),
        List(
          Team(id = 2, name = "Team 5", speaker = "Tiberiu", Duration.ofSeconds(45)),
          Team(id = 3, name = "Team 5", speaker = "Alan", Duration.ofSeconds(45)),
          Team(id = 4, name = "Team 5", speaker = "Alejandro", Duration.ofSeconds(45)),
          Team(id = 5, name = "Team 5", speaker = "Raaj", Duration.ofSeconds(45)),
          Team(id = 6, name = "Team 5", speaker = "Ajay", Duration.ofSeconds(45)),
          Team(id = 7, name = "Team 5", speaker = "Jeremy", Duration.ofSeconds(45))
        )
      )),
      Standup(id = 4, name = "dev", displayName="Access UK Dev Symposium",teams = NonEmptyList(
        Team(id = 1, name = "Releases", speaker = "Steff", Duration.ofSeconds(90)),
        List(
          Team(id = 2, name = "L3", speaker = "David", Duration.ofSeconds(45)),
          Team(id = 3, name = "CWI", speaker = "Shiv", Duration.ofSeconds(45)),
          Team(id = 4, name = "Out of Country", speaker = "Alua", Duration.ofSeconds(45)),
          Team(id = 5, name = "Standard Sections", speaker = "Daniel N", Duration.ofSeconds(45)),
          Team(id = 6, name = "Tech CI", speaker = "Dom / Parvez", Duration.ofSeconds(45)),
          Team(id = 7, name = "CI", speaker = "Rahul / Elliot", Duration.ofSeconds(45)),
          Team(id = 8, name = "FES", speaker = "Daniel T", Duration.ofSeconds(45)),
          Team(id = 9, name = "Team 5", speaker = "Alan / Alejandro", Duration.ofSeconds(45)),
          Team(id = 10, name = "EEA FP", speaker = "Ethan / Adam", Duration.ofSeconds(45))
        )
      ))
    )
  }

}
