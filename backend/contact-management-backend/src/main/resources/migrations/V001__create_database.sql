USE master;
GO

CREATE LOGIN cms_user
WITH PASSWORD = 'Cms@123456';
GO

USE ContactManagementDB;
GO

CREATE USER cms_user FOR LOGIN cms_user;
GO

ALTER ROLE db_owner ADD MEMBER cms_user;
GO