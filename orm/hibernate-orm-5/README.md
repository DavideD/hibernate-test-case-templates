1. Start PostgresSQL:
   ```
   docker run --rm --name SOQuestionPg -e POSTGRES_USER=hibernate -e POSTGRES_PASSWORD=hibernate -e POSTGRES_DB=hibernate -p 5432:5432 postgres:13.2
   ```
2. Run queriees:
   ```
   create schema "user";
   CREATE TABLE "user"."Authorities"(  id integer NOT NULL,  name text COLLATE pg_catalog."default" NOT NULL,  CONSTRAINT "Authorities_pkey" PRIMARY KEY (id));
   ```
3. Run test `ORMUnitTestCase#hhh123Test`:
   ```
   mvn test -Dtest=ORMUnitTestCase
   ```
   