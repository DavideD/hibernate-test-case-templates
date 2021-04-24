create schema "user";
CREATE TABLE "user"."Authorities"(  id integer NOT NULL,  name text COLLATE pg_catalog."default" NOT NULL,  CONSTRAINT "Authorities_pkey" PRIMARY KEY (id));