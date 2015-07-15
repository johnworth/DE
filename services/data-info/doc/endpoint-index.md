# Endpoint Index

* [GET /status](endpoints/service-info.md)
* [POST /cart](endpoints/cart.md)

## Unmigrated Endpoints

* [GET /admin/filesystem/index](endpoints/filesystem/search.md#endpoints:)
* [POST /admin/filesystem/index](endpoints/filesystem/search.md#endpoint)
* [POST /fileio/upload](endpoints/fileio.md#uploading)
* [GET /secured/admin/config](endpoints/admin.md#listing-the-config-for-data-info-)
* [GET /secured/admin/status](endpoints/admin.md#status-check-)
* [GET /secured/fileio/download](endpoints/fileio.md#downloading)
* [POST /secured/fileio/save](endpoints/fileio.md#save)
* [POST /secured/fileio/saveas](endpoints/fileio.md#save-as)
* [POST /secured/fileio/urlupload](endpoints/fileio.md#url-uploads)
* [POST /secured/filesystem/anon-files](endpoints/filesystem/sharing.md#sharing-files-with-the-anonymous-user-)
* [POST /secured/filesystem/delete](endpoints/filesystem/delete.md#deleting-files-andor-directories-)
* [POST /secured/filesystem/delete-contents](endpoints/filesystem/delete.md#deleting-all-items-in-a-directory-)
* [POST /secured/filesystem/delete-tickets](endpoints/filesystem/tickets.md#deleting-tickets-)
* [GET /secured/filesystem/file/manifest](endpoints/filesystem/manifest.md#file-manifest-)
* [GET /secured/filesystem/file/preview](endpoints/filesystem/preview.md#file-preview-)
* [GET /secured/filesystem/groups](endpoints/filesystem/groups.md#listing-a-users-group-memberships-)
* [POST /secured/filesystem/list-tickets](endpoints/filesystem/tickets.md#listing-tickets-)
* [DELETE /secured/filesystem/metadata](endpoints/filesystem/metadata.md#deleting-file-and-directory-metadata-)
* [GET /secured/filesystem/metadata](endpoints/filesystem/metadata.md#getting-metadata-)
* [POST /secured/filesystem/metadata](endpoints/filesystem/metadata.md#setting-metadata-)
* [POST /secured/filesystem/metadata-batch](endpoints/filesystem/metadata.md#setting-metadata-as-a-batch-operation-)
* [POST /secured/filesystem/move](endpoints/filesystem/move.md#moving-files-andor-directories-)
* [POST /secured/filesystem/move-contents](endpoints/filesystem/move.md#moving-all-items-in-a-directory-)
* [POST /secured/filesystem/overwrite-chunk](endpoints/filesystem/overwrite-chunk.md#overwriting-a-chunk-of-a-file-)
* [GET /secured/filesystem/paged-directory](endpoints/filesystem/directory-listing.md#paged-directory-listing-)
* [POST /secured/filesystem/paths-contain-space](endpoints/filesystem/paths-contain-space.md#checking-for-spaces-in-paths-andor-directories-)
* [POST /secured/filesystem/paths-for-uuids](endpoints/filesystem/uuids.md#paths-for-uuids-)
* [GET /secured/filesystem/quota](endpoints/filesystem/quotas.md#listing-a-users-quotas-)
* [POST /secured/filesystem/read-chunk](endpoints/filesystem/read-chunk.md#reading-a-chunk-of-a-file-)
* [POST /secured/filesystem/read-csv-chunk](endpoints/filesystem/csv-tsv-parsing.md#csvtsv-parsing-)
* [POST /secured/filesystem/rename](endpoints/filesystem/rename.md#renaming-a-file-or-directory-)
* [POST /secured/filesystem/replace-spaces](endpoints/filesystem/replace-spaces.md#replacing-spaces-with-underscores-)
* [POST /secured/filesystem/restore](endpoints/filesystem/restore.md#restoring-a-file-or-directory-from-a-users-trash-)
* [POST /secured/filesystem/restore-all](endpoints/filesystem/restore.md#restoring-all-items-in-a-users-trash-)
* [GET /secured/filesystem/root](endpoints/filesystem/root-listing.md#top-level-root-listing-)
* [POST /secured/filesystem/share](endpoints/filesystem/sharing.md#sharing-)
* [POST /secured/filesystem/tickets](endpoints/filesystem/tickets.md#creating-tickets-)
* [DELETE /secured/filesystem/trash](endpoints/filesystem/empty-trash.md#emptying-a-users-trash-directory-)
* [POST /secured/filesystem/unshare](endpoints/filesystem/sharing.md#unsharing-)
* [POST /secured/filesystem/user-permissions](endpoints/filesystem/permissions.md#listing-user-permissions-)
* [GET /secured/filesystem/user-trash-dir](endpoints/filesystem/user-trash-dir.md#getting-the-path-to-a-users-trash-directory--)
* [POST /secured/filesystem/uuids-for-paths](endpoints/filesystem/uuids.md#uuids-for-paths-)
* [GET /secured/filetypes/auto-type](endpoints/filetypes.md#preview-what-the-automatically-assigned-type-would-be-)
* [POST /secured/filetypes/auto-type](endpoints/filetypes.md#automatically-assign-a-type-to-a-file-)
* [DELETE /secured/filetypes/type](endpoints/filetypes.md#delete-a-file-type-from-a-file-)
* [GET /secured/filetypes/type](endpoints/filetypes.md#get-the-file-type-associated-with-a-file-)
* [POST /secured/filetypes/type](endpoints/filetypes.md#add-a-file-type-to-a-file-)
* [GET /secured/filetypes/type-list](endpoints/filetypes.md#get-the-list-of-supported-file-types-)
* [GET /secured/filetypes/type/paths](endpoints/filetypes.md#look-up-paths-in-a-users-home-directory-based-on-file-type-)