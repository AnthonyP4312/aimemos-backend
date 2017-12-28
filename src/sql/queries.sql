--:name add-user :! :n
INSERT INTO users (username, password, status)
VALUES (:username, :password, :status::status);

--:name add-buddy :! :n
INSERT INTO buddies (username, buddyname, groupname)
VALUES (:username, :buddyname, :groupname);

--:name delete-buddy :! :n
DELETE FROM buddies
WHERE username = :username AND buddyname = :buddyname

--:name delete-user :! :n
DELETE FROM users
WHERE username = :username

--:name buddies-by-user :? :*
SELECT u.username, b.groupname, u.status 
FROM users AS u
JOIN (SELECT buddyname, groupname FROM buddies WHERE username = :username) AS b
ON u.username = b.buddyname

--:name users-by-buddy :? :*
SELECT username
FROM buddies
WHERE buddyname = :buddyname

--:name password-by-user :? :1
SELECT password
FROM users
WHERE username = :username

--:name update-status :! :n
UPDATE users
SET status = :status::status
WHERE username = :username

--:name update-groupname :! :n
--:doc (db {:username :buddyname})
UPDATE buddies
SET groupname = :groupname
WHERE username = :username AND buddyname = :buddyname
