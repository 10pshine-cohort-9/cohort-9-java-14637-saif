USE master;
GO

-- Replace <YOUR_SECURE_PASSWORD> with a strong password before executing.
-- Do not commit real credentials to source control.
CREATE LOGIN cms_user
WITH PASSWORD = '<YOUR_SECURE_PASSWORD>';
GO

USE ContactManagementDB;
GO

CREATE USER cms_user FOR LOGIN cms_user;
GO

ALTER ROLE db_owner ADD MEMBER cms_user;
GO