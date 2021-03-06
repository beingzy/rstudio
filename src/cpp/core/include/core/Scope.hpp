/*
 * Scope.hpp
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#ifndef CORE_SCOPE_HPP
#define CORE_SCOPE_HPP


namespace core {
namespace scope {

template <class T>
class SetOnExit
{
public:
   SetOnExit(T* pLocation, const T& value)
   {
      pLocation_ = pLocation;
      value_ = value;
   }

   virtual ~SetOnExit()
   {
      try
      {
         *pLocation_ = value_;
      }
      catch(...)
      {
      }
   }

 private:
   T* pLocation_;
   T value_;
};

} // namespace scope
} // namespace core 


#endif // CORE_SCOPE_HPP

