/**
 * Japt-Proxy: The JAVA(TM) based APT-Proxy
 *
 * Copyright (C) 2006-2008  Oliver Siegmar <oliver@siegmar.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.siegmar.japtproxy.packagepool;

import net.siegmar.japtproxy.packages.RepoPackage;
import net.siegmar.japtproxy.packages.RepoPackageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test
@ContextConfiguration(locations = { "classpath:test-master.xml" })
public class DebianPackageTest extends AbstractTestNGSpringContextTests {

    @Autowired
    @Qualifier("debianRepoPackageFactory")
    private RepoPackageBuilder repoPackageBuilder;

    public void testPackage() throws Exception {
        final RepoPackage debianPackage = repoPackageBuilder.newPackage("dummy_1.4pre.20050518_i386.deb");
        assertEquals(debianPackage.getBasename(), "dummy");
        assertEquals(debianPackage.getVersion(), "1.4pre.20050518");
        assertNull(debianPackage.getRevision());
        assertEquals(debianPackage.getArch(), "i386");
        assertEquals(debianPackage.getExtension(), "deb");
        assertTrue(debianPackage.isImmutable());
    }

}
