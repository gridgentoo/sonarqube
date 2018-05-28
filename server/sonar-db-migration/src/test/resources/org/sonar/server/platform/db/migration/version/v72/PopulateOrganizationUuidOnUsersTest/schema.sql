CREATE TABLE "USERS" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "UUID" VARCHAR(255) NOT NULL,
  "LOGIN" VARCHAR(255) NOT NULL,
  "NAME" VARCHAR(200),
  "EMAIL" VARCHAR(100),
  "CRYPTED_PASSWORD" VARCHAR(100),
  "SALT" VARCHAR(40),
  "HASH_METHOD" VARCHAR(10),
  "ACTIVE" BOOLEAN DEFAULT TRUE,
  "SCM_ACCOUNTS" VARCHAR(4000),
  "EXTERNAL_ID" VARCHAR(255) NOT NULL,
  "EXTERNAL_LOGIN" VARCHAR(255) NOT NULL,
  "EXTERNAL_IDENTITY_PROVIDER" VARCHAR(100) NOT NULL,
  "IS_ROOT" BOOLEAN NOT NULL,
  "USER_LOCAL" BOOLEAN,
  "ONBOARDED" BOOLEAN NOT NULL,
  "HOMEPAGE_TYPE" VARCHAR(40),
  "HOMEPAGE_PARAMETER" VARCHAR(40),
  "ORGANIZATION_UUID" VARCHAR(40),
  "CREATED_AT" BIGINT,
  "UPDATED_AT" BIGINT
);
CREATE UNIQUE INDEX "USERS_UUID" ON "USERS" ("UUID");
CREATE UNIQUE INDEX "USERS_LOGIN" ON "USERS" ("LOGIN");
CREATE UNIQUE INDEX "UNIQ_EXTERNAL_ID" ON "USERS" ("EXTERNAL_IDENTITY_PROVIDER", "EXTERNAL_ID");
CREATE INDEX "USERS_UPDATED_AT" ON "USERS" ("UPDATED_AT");

CREATE TABLE "ORGANIZATIONS" (
  "UUID" VARCHAR(40) NOT NULL PRIMARY KEY,
  "KEE" VARCHAR(32) NOT NULL,
  "NAME" VARCHAR(64) NOT NULL,
  "DESCRIPTION" VARCHAR(256),
  "URL" VARCHAR(256),
  "AVATAR_URL" VARCHAR(256),
  "GUARDED" BOOLEAN NOT NULL,
  "USER_ID" INTEGER,
  "DEFAULT_PERM_TEMPLATE_PROJECT" VARCHAR(40),
  "DEFAULT_PERM_TEMPLATE_VIEW" VARCHAR(40),
  "DEFAULT_GROUP_ID" INTEGER,
  "DEFAULT_QUALITY_GATE_UUID" VARCHAR(40) NOT NULL,
  "NEW_PROJECT_PRIVATE" BOOLEAN NOT NULL,
  "CREATED_AT" BIGINT NOT NULL,
  "UPDATED_AT" BIGINT NOT NULL
);
CREATE UNIQUE INDEX "PK_ORGANIZATIONS" ON "ORGANIZATIONS" ("UUID");
CREATE UNIQUE INDEX "ORGANIZATION_KEY" ON "ORGANIZATIONS" ("KEE");