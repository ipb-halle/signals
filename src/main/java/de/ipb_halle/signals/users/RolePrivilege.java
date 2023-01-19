/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.signals.users;


/** 
 * SNB role privileges
 */

public enum RolePrivilege { 
    canManageSystemTemplates,
    canMoveExperiments,
    canCreateParaExp,
    canRetireContainer,
    canTrashPlate,
    canTrashMaterials,
    canRunAndPrintInventoryReports,
    canTrashNotebooks,
    canTrashLocation,
    canReturnFromArchive,
    canAddContainer,
    canManageMaterialLibraries,
    canShareTemplates,
    canUpdateContainerAmount,
    canExportParaExp,
    canRetirePlate,
    canTrashSamples,
    canManageAttributes,
    canManageGroups,
    canSearchElnArchive,
    canConfigure,
    canAddLocation,
    canTrashRequests,
    canAddPlate,
    canRequestContainer,
    canRequestPlate,
    canArchive,
    canAddMaterials,
    canShare,
    canTrashContainer,
    canTrashExperiments,
    canEditContainer,
    canMoveParaExp,
    canViewMaterials,
    canTrashMandatoryEntities,
    canManageAdminDefinedObject,
    canEditPlate,
    canAccessInventoryApp,
    canEditLocation,
    canExportNotebooks,
    canEditMaterials,
    canExportMaterials,
    canTrashParaExp,
    manageVitroVivo,
    manageInventa
}
